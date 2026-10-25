-- Kashif backend schema (Supabase free tier). Only hashed numbers are stored; contacts are never uploaded.
create table if not exists spam_reports (
  id bigserial primary key,
  number_hash text not null check (length(number_hash)=64),
  hash_prefix text generated always as (substr(number_hash,1,6)) stored,
  category text not null check (category in ('spam','scam','telemarketing','fraud_bank','prize','other')),
  reporter uuid references auth.users(id) on delete set null,
  created_at timestamptz not null default now(),
  unique (number_hash, reporter)
);
create index if not exists spam_reports_prefix_idx on spam_reports(hash_prefix);
create table if not exists businesses (
  id bigserial primary key,
  number_hash text not null unique,
  name text not null,
  verified boolean not null default false,
  created_at timestamptz not null default now()
);
create table if not exists profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  username text unique,
  tier text not null default 'free' check (tier in ('free','pro')),
  is_admin boolean not null default false
);
alter table spam_reports enable row level security;
alter table businesses enable row level security;
alter table profiles enable row level security;
create policy "insert own report" on spam_reports for insert to authenticated with check (reporter = auth.uid());
create policy "read businesses" on businesses for select using (true);
create policy "own profile" on profiles for select using (id = auth.uid());
-- Hash-prefix lookup: client sends 6 hex chars, gets aggregated counts, never the raw number.
create or replace function lookup_prefix(p text) returns table(number_hash text, reports bigint) language sql security definer as $$
  select number_hash, count(*) from spam_reports where hash_prefix = p group by number_hash;
$$;
