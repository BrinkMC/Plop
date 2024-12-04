# Plot Schema
create table if not exists `PersonalPlot`
(
    plot_id varchar(36) not null,
    player_id varchar(36) not null,
    claim_id varchar(36) not null,
    plot_visit_id varchar(36) not null,
    primary key (plot_id, player_id),
    foreign key (claim_id) references Claim(claim_id),
    foreign key (plot_visit_id) references PlotVisit(plot_visit_id)
);

create table if not exists `GuildPlot`
(
    plot_id  varchar(36) not null,
    guild_id varchar(36) not null,
    claim_id varchar(36) not null,
    plot_visit_id varchar(36) not null,
    primary key (plot_id, guild_id),
    foreign key (claim_id) references Claim(claim_id),
    foreign key (plot_visit_id) references PlotVisit(plot_visit_id)
    );

create table if not exists `GuildMember`
(
    member_id varchar(36) not null,
    guild_plot_id varchar(36) not null,
    primary key (member_id),
    foreign key (guild_plot_id) references GuildPlot(plot_id)
);

create table if not exists `Claim`
(
    claim_id varchar(36) not null,
    max_length int not null,
    centre text not null,
    home text null,
    visit text null,
    primary key (claim_id)
);

create table if not exists `Totem`
(
    totem_id varchar(36) null,
    totem_type text not null,
    totem_location text not null,
    personal_plot_id varchar(36) null, # I can't have both
    guild_plot_id varchar(36) null,
    foreign key (personal_plot_id) references PersonalPlot(plot_id),
    foreign key (guild_plot_id) references GuildPlot(plot_id),
    check (
        (personal_plot_id is not null and guild_plot_id is null) or
        (guild_plot_id is not null and personal_plot_id is null)
    )
);

create table if not exists `PlotSize`
(
    level int not null,
    personal_plot_id varchar(36) null, # I can't have both
    guild_plot_id varchar(36) null,
    foreign key (personal_plot_id) references PersonalPlot(plot_id),
    foreign key (guild_plot_id) references GuildPlot(plot_id),
    check (
        (personal_plot_id is not null and guild_plot_id is null) or
        (guild_plot_id is not null and personal_plot_id is null)
    )
);

create table if not exists `FactoryLimit`
(
    level int not null,
    personal_plot_id varchar(36) null, # I can't have both
    guild_plot_id varchar(36) null,
    foreign key (personal_plot_id) references PersonalPlot(plot_id),
    foreign key (guild_plot_id) references GuildPlot(plot_id),
    check (
        (personal_plot_id is not null and guild_plot_id is null) or
        (guild_plot_id is not null and personal_plot_id is null)
        )
);

create table if not exists `ShopLimit`
(
    level int not null,
    personal_plot_id varchar(36) null, # I can't have both
    guild_plot_id varchar(36) null,
    foreign key (personal_plot_id) references PersonalPlot(plot_id),
    foreign key (guild_plot_id) references GuildPlot(plot_id),
    check (
        (personal_plot_id is not null and guild_plot_id is null) or
        (guild_plot_id is not null and personal_plot_id is null)
        )
);

create table if not exists `VisitorLimit`
(
    level int not null,
    personal_plot_id varchar(36) null, # I can't have both
    guild_plot_id varchar(36) null,
    foreign key (personal_plot_id) references PersonalPlot(plot_id),
    foreign key (guild_plot_id) references GuildPlot(plot_id),
    check (
        (personal_plot_id is not null and guild_plot_id is null) or
        (guild_plot_id is not null and personal_plot_id is null)
        )
);

create table if not exists `PlotVisit`
(
    plot_visit_id varchar(36) not null,
    allow_visitors binary(1) not null
);

create table if not exists `Visit`
(
    visit_id int auto_increment primary key,
    plot_visit_id varchar(36) not null,
    visit_timestamp timestamp not null default current_timestamp,
    foreign key (plot_visit_id) references PlotVisit(plot_visit_id)
);

# Shop Schema
create table if not exists `Shop`
(

);