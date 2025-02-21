CREATE TABLE IF NOT EXISTS `plots` (
    `plot_id` VARCHAR(36) NOT NULL,
    `type` ENUM('PERSONAL','GUILD') NOT NULL,
    PRIMARY KEY (`plot_id`)
    );

CREATE TABLE IF NOT EXISTS `plot_claims` (
    `plot_id` VARCHAR(36) NOT NULL,
    `centre` TEXT NOT NULL,
    `home` TEXT NOT NULL,
    `visit` TEXT NOT NULL,
    PRIMARY KEY (`plot_id`),
    FOREIGN KEY (`plot_id`) REFERENCES `plots`(`plot_id`)
    );

CREATE TABLE IF NOT EXISTS `plot_sizes` (
    `plot_id` VARCHAR(36) NOT NULL,
    `size_level` INT NOT NULL,
    PRIMARY KEY (`plot_id`),
    FOREIGN KEY (`plot_id`) REFERENCES `plots`(`plot_id`)
    );

CREATE TABLE IF NOT EXISTS `plot_factories` (
    `plot_id` VARCHAR(36) NOT NULL,
    `factory_level` INT NOT NULL,
    PRIMARY KEY (`plot_id`),
    FOREIGN KEY (`plot_id`) REFERENCES `plots`(`plot_id`)
    );

CREATE TABLE IF NOT EXISTS `plot_factory_locations` (
    `factory_id` INT AUTO_INCREMENT NOT NULL,
    `plot_id` VARCHAR(36) NOT NULL,
    `factory_location` TEXT NOT NULL,
    PRIMARY KEY (`factory_id`),
    FOREIGN KEY (`plot_id`) REFERENCES `plots`(`plot_id`)
    );

CREATE TABLE IF NOT EXISTS `plot_shops` (
    `plot_id` VARCHAR(36) NOT NULL,
    `shop_level` INT NOT NULL,
    PRIMARY KEY (`plot_id`),
    FOREIGN KEY (`plot_id`) REFERENCES `plots`(`plot_id`)
    );

CREATE TABLE IF NOT EXISTS `plot_shop_locations` (
    `shop_loc_id` INT AUTO_INCREMENT NOT NULL,
    `plot_id` VARCHAR(36) NOT NULL,
    `shop_uuid` VARCHAR(36) NOT NULL,
    PRIMARY KEY (`shop_loc_id`),
    FOREIGN KEY (`plot_id`) REFERENCES `plots`(`plot_id`)
    );

CREATE TABLE IF NOT EXISTS `plot_totem_levels` (
    `plot_id` VARCHAR(36) NOT NULL,
    `totem_level` INT NOT NULL,
    PRIMARY KEY (`plot_id`),
    FOREIGN KEY (`plot_id`) REFERENCES `plots`(`plot_id`)
    );

CREATE TABLE IF NOT EXISTS `plot_totems` (
    `totem_id` INT AUTO_INCREMENT NOT NULL,
    `plot_id` VARCHAR(36) NOT NULL,
    `totem_type` TEXT NOT NULL,
    `totem_location` TEXT NOT NULL,
    PRIMARY KEY (`totem_id`),
    FOREIGN KEY (`plot_id`) REFERENCES `plots`(`plot_id`)
    );

CREATE TABLE IF NOT EXISTS `plot_visits` (
    `plot_id` VARCHAR(36) NOT NULL,
    `allow_visitors` BINARY(1) NOT NULL,
    `visit_level` INT NOT NULL,
    `current_visits` INT NOT NULL,
    PRIMARY KEY (`plot_id`),
    FOREIGN KEY (`plot_id`) REFERENCES `plots`(`plot_id`)
    );

CREATE TABLE IF NOT EXISTS `plot_visit_timestamps` (
    `record_id` INT AUTO_INCREMENT NOT NULL,
    `plot_id` VARCHAR(36) NOT NULL,
    `visit_timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`record_id`),
    FOREIGN KEY (`plot_id`) REFERENCES `plots`(`plot_id`)
    );