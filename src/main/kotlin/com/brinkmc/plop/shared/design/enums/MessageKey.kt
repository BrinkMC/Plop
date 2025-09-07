package com.brinkmc.plop.shared.design.enums

enum class MessageKey(val key: String) {

    NO_PERMISSION("no_permission"),
    NO_MONEY("no_money"), // Only use for plots?
    NO_PLOT("no_plot"),

    // Preview
    PREVIEW_IN_PROGRESS("plot.preview.in_progress"),
    NO_PREVIEW("plot.preview.none"),


    // Plot messages
    NOT_PLOT("plot.no_plot"),
    NOT_OWNER("plot.not-owner"),
    NOT_VISITABLE("plot.not_visitable"),
    PLOT_FULL("plot.full"),
    TOTEM_LIMIT("plot.totem.limit"),
    NO_GUILD("plot.guild.none"),
    GUILD_TOO_SMALL("plot.guild.too_small"),
    NOT_GUILD_MASTER("plot.guild.not_master"),
    MAX_PLOTS_REACHED("plot.max_plots_reached"),
    HAS_PLOTS_PERSONAL("plot.has_plots.personal"),
    HAS_PLOTS_GUILD("plot.has_plots.guild"),

    // Plot success messages
    CLAIM_SUCCESS("plot.claim.success"),
    DELETE_SUCCESS("plot.delete.success"),
    PLOT_SET_ENTRANCE("plot.set.entrance"),
    PLOT_SET_HOME("plot.set.visit"),
    UPGRADE_SUCCESS("plot.upgrade.success"),
    PLOT_TOGGLE_VISIT("plot.toggle.visit"),

    // Shop messages
    PLAYER_INSUFFICIENT_BALANCE("shop.purchase.player.insufficient.balance"),
    SHOP_INSUFFICIENT_BALANCE("shop.purchase.insufficient.balance"),
    PLAYER_INSUFFICIENT_STOCK("shop.purchase.player.insufficient.stock"),
    SHOP_INSUFFICIENT_STOCK("shop.purchase.insufficient.stock"),
    SHOP_BUY_LIMIT_REACHED("shop.purchase.insufficient.buy_limit"),

    SHOP_PURCHASE_SUCCESSFUL("shop.purchase.successful"),

    // Teleport
    TELEPORT_IN_PROGRESS("plot.teleport.progress"),
    TELEPORT_INTERRUPTED("plot.teleport.interrupted"),
    TELEPORT_FAILED("plot.teleport.failed"),
    TELEPORT_COMPLETE("plot.teleport.complete"),

    COOLDOWN("preview.cooldown"),

    // Nexus
    NEXUS_BOOK_NAME("nexus.book.name"),
    NEXUS_BOOK_DESC("nexus.book.desc"),
    NEXUS_BOOK_PRESENT("nexus.book.have"),
    NEXUS_BOOK_GIVEN("nexus.book.given"),

    // GUIs
    MENU_BUY_TITLE("gui.buy_player.title"),


    MENU_CREATE_TITLE("gui.create_shop.title"),
    MENU_CREATE_CHOOSE_ITEM_NAME("gui.create_shop.choose_item.name"),
    MENU_CREATE_CHOOSE_ITEM_DESC("gui.create_shop.choose_item.desc"),
    MENU_CREATE_NO_ITEM_NAME("gui.create_shop.no_item.name"),
    MENU_CREATE_NO_ITEM_DESC("gui.create_shop.no_item.desc"),
    MENU_CREATE_NO_BUY_SELL_NAME("gui.create_shop.no_buy_sell.name"),
    MENU_CREATE_NO_BUY_SELL_DESC("gui.create_shop.no_buy_sell.desc"),

    MENU_NEXUS_TITLE("gui.nexus.title"),
    MENU_NEXUS_ICON_NAME("gui.nexus.icon.name"),
    MENU_NEXUS_ICON_DESC("gui.nexus.icon.desc"),
    MENU_NEXUS_UPGRADE_NAME("gui.nexus.upgrade.name"),
    MENU_NEXUS_UPGRADE_DESC("gui.nexus.upgrade.desc"),
    MENU_NEXUS_TOTEM_NAME("gui.nexus.totem.name"),
    MENU_NEXUS_TOTEM_DESC("gui.nexus.totem.desc"),

    // General UI Items
    MENU_ERROR_NAME("gui.error.name"),
    MENU_ERROR_DESC("gui.error.desc"),

    MENU_ZERO_AMOUNT_NAME("gui.amount.zero.name"),
    MENU_ZERO_AMOUNT_DESC("gui.amount.zero.desc"),
    MENU_BAD_AMOUNT_NAME("gui.amount.bad.name"),
    MENU_BAD_AMOUNT_DESC("gui.amount.bad.desc"),

    MENU_PLAYER_INSUFFICIENT_BALANCE_NAME("gui.amount.player.insufficient.name"),
    MENU_PLAYER_INSUFFICIENT_BALANCE_DESC("gui.amount.player.insufficient.desc"),
    MENU_SHOP_INSUFFICIENT_BALANCE_NAME("gui.amount.shop.insufficient.name"),
    MENU_SHOP_INSUFFICIENT_BALANCE_DESC("gui.amount.shop.insufficient.desc"),
    MENU_PLAYER_INSUFFICIENT_STOCK_NAME("gui.amount.player.insufficient.name"),
    MENU_PLAYER_INSUFFICIENT_STOCK_DESC("gui.amount.player.insufficient.desc"),
    MENU_SHOP_INSUFFICIENT_STOCK_NAME("gui.amount.shop.insufficient.name"),
    MENU_SHOP_INSUFFICIENT_STOCK_DESC("gui.amount.shop.insufficient.desc"),

    MENU_BUY_LIMIT_REACHED_NAME("gui.amount.buy_limit.name"),
    MENU_BUY_LIMIT_REACHED_DESC("gui.amount.buy_limit.desc"),

    MENU_CONFIRM_BUY_NAME("gui.confirm.buy.name"),
    MENU_CONFIRM_BUY_DESC("gui.confirm.buy.desc"),

    MENU_MORE_NAME("gui.more.name"),
    MENU_MORE_DESC("gui.more.desc"),
    MENU_LESS_NAME("gui.less.name"),
    MENU_LESS_DESC("gui.less.desc"),

    PREVIEW_ERROR_GUILD_SMALL("preview.error.guild-too-small"),
}