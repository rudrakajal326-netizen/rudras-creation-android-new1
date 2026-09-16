-- Rudras Creation app/CRM extension.
-- Run AFTER the supplied SUPABASE_SETUP.sql.

create table if not exists public.store_customers (
  user_id uuid primary key references auth.users(id) on delete cascade,
  full_name text,
  phone text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
alter table public.store_customers enable row level security;
drop policy if exists "customer own profile" on public.store_customers;
create policy "customer own profile" on public.store_customers for all using (auth.uid()=user_id) with check (auth.uid()=user_id);

create table if not exists public.store_coupons (
  id uuid primary key default gen_random_uuid(),
  code text unique not null,
  discount_type text not null check(discount_type in ('percent','fixed')),
  discount_value numeric(12,2) not null check(discount_value >= 0),
  min_order numeric(12,2) not null default 0,
  max_discount numeric(12,2),
  starts_at timestamptz,
  ends_at timestamptz,
  usage_limit integer,
  used_count integer not null default 0,
  active boolean not null default true,
  created_at timestamptz not null default now()
);
alter table public.store_coupons enable row level security;
drop policy if exists "public coupon validation" on public.store_coupons;
create policy "public coupon validation" on public.store_coupons for select using (active=true and (starts_at is null or starts_at<=now()) and (ends_at is null or ends_at>=now()));
drop policy if exists "admin coupons" on public.store_coupons;
create policy "admin coupons" on public.store_coupons for all using (public.is_store_admin()) with check (public.is_store_admin());

create or replace function public.validate_coupon(p_code text,p_subtotal numeric)
returns jsonb language plpgsql security definer set search_path=public as $$
declare c store_coupons; d numeric;
begin
  select * into c from store_coupons where upper(code)=upper(trim(p_code)) and active=true and (starts_at is null or starts_at<=now()) and (ends_at is null or ends_at>=now()) and p_subtotal>=min_order and (usage_limit is null or used_count<usage_limit) limit 1;
  if not found then return jsonb_build_object('valid',false,'discount',0,'message','Invalid or ineligible coupon'); end if;
  d=case when c.discount_type='percent' then p_subtotal*c.discount_value/100 else c.discount_value end;
  if c.max_discount is not null then d=least(d,c.max_discount); end if;
  return jsonb_build_object('valid',true,'discount',greatest(0,d),'code',c.code);
end $$;

-- Server-side order validation: verifies SKUs/prices against the live catalog.
create or replace function public.create_verified_order(
 p_order_id text,p_customer_name text,p_phone text,p_email text,p_address text,p_city text,p_state text,p_pincode text,
 p_delivery_method text,p_payment_method text,p_items jsonb,p_notes text default ''
) returns uuid language plpgsql security definer set search_path=public as $$
declare v uuid; i jsonb; dbp store_products; server_total numeric:=0; q integer;
begin
 if length(trim(p_customer_name))<2 or length(regexp_replace(p_phone,'\D','','g'))<10 then raise exception 'Invalid customer details'; end if;
 if jsonb_typeof(p_items)<>'array' or jsonb_array_length(p_items)=0 then raise exception 'Cart is empty'; end if;
 for i in select * from jsonb_array_elements(p_items) loop
   select * into dbp from store_products where sku=i->>'sku' for share;
   if not found then raise exception 'Product unavailable: %', i->>'sku'; end if;
   q=greatest(1,least(coalesce((i->>'qty')::integer,1),99));
   if dbp.inventory is not null and coalesce((select sum(value::numeric) from jsonb_each_text(dbp.inventory)),0)<q then raise exception 'Insufficient stock for %', dbp.name; end if;
   server_total=server_total+(dbp.price*q);
 end loop;
 insert into store_orders(order_id,customer_name,phone,email,address,city,state,pincode,delivery_method,payment_method,items,total,notes)
 values(p_order_id,left(trim(p_customer_name),120),left(trim(p_phone),40),nullif(left(trim(coalesce(p_email,'')),160),''),left(trim(p_address),500),left(trim(p_city),80),left(trim(p_state),80),left(trim(p_pincode),20),left(trim(p_delivery_method),80),left(trim(p_payment_method),80),p_items,server_total,left(coalesce(p_notes,''),1000))
 on conflict(order_id) do nothing returning id into v;
 return v;
end $$;
