# Plot Schema
create table if not exists `plots_plots`
(
    plot_id varchar(36) not null,
    type enum('Personal', 'Guild') not null,
    owner_id varchar(36) not null,
    primary key (plot_id)
);

create table if not exists `plots_claims`
(
    centre text not null,
    home text null,
    visit text null,
    plot_id varchar(36) not null,
    primary key (plot_id),
    foreign key (plot_id) references plots_plots(plot_id)
);

create table if not exists `plots_totems`
(
    totem_id int auto_increment not null,
    totem_location text      not null,
    totem_type     text        not null,
    plot_id        varchar(36) not null,
    primary key (totem_id),
    foreign key (plot_id) references plots_plots(plot_id)
);

create table if not exists `plots_sizes`
(
    level int not null,
    plot_id varchar(36) not null,
    primary key (plot_id),
    foreign key (plot_id) references plots_plots(plot_id)
);

create table if not exists `plots_factory_limits`
(
    level int not null,
    plot_id varchar(36) not null,
    primary key (plot_id),
    foreign key (plot_id) references plots_plots(plot_id)
);

create table if not exists `plots_factory_locations`
(
    factory_id int auto_increment not null,
    factory_location text not null,
    plot_id varchar(36) not null,
    primary key (factory_id),
    foreign key (plot_id) references plots_plots(plot_id)
);

create table if not exists `plots_shop_limits`
(
    level int not null,
    plot_id varchar(36) not null,
    primary key (plot_id),
    foreign key (plot_id) references plots_plots(plot_id)
);

create table if not exists `plots_shop_locations`
(
    shop_id varchar(36) not null,
    plot_id varchar(36) not null,
    primary key (shop_id),
    foreign key (plot_id) references plots_plots(plot_id)
);

create table if not exists `plots_visitor_limits`
(
    allow_visitors binary not null,
    level int not null,
    current_amount int,
    plot_id varchar(36) not null,
    primary key (plot_id),
    foreign key (plot_id) references plots_plots(plot_id)
);

create table if not exists `plots_visit_records`
(
    visit_record_id int auto_increment primary key not null,
    plot_id varchar(36) not null,
    visit_timestamp timestamp not null default current_timestamp,
    foreign key (plot_id) references plots_plots(plot_id)
);

# Shop Schema
create table if not exists `shops_shops`
(
    shop_id varchar(36) not null,
    loc varchar(36) not null,
    owner_id varchar(36) not null,
    ware text,
    stock int,
    price float,
    primary key (shop_id)
);

create table if not exists `saved_inventories`
(
    player_uuid varchar(36) not null,
    inventory text,
    primary key (player_uuid)
);