create database IF NOT EXISTS `odour_collect_db`;

use odour_collect_db;

DROP TABLE IF EXISTS `report_oc`;
DROP TABLE IF EXISTS `user_oc`;

CREATE TABLE IF NOT EXISTS `user_oc` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`username` varchar(255) UNIQUE NOT NULL,
`email` varchar(255) NOT NULL,
`password` varchar(255) NOT NULL,
`age` varchar(255) NOT NULL,
`gender` varchar(255) NOT NULL,
`signup_date` TIMESTAMP,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

CREATE TABLE IF NOT EXISTS `report_oc` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`type` varchar(255) NOT NULL,
`intensity` int(2) NOT NULL,
`annoyance` int(2) NOT NULL,
`cloud` int(2) NOT NULL,
`rain` int(2) NOT NULL,
`wind` int(2) NOT NULL,
`origin` varchar(255) NOT NULL,
`duration` varchar(255) NOT NULL,
`pic` LONGBLOB, 
`report_date` TIMESTAMP,
`latlng` varchar(255) NOT NULL,
`latitude` DECIMAL(10, 8) NOT NULL, 
`longitude` DECIMAL(11, 8) NOT NULL,
`user_id` int(11) NOT NULL,
PRIMARY KEY (`id`),
FOREIGN KEY (`user_id`) REFERENCES user_oc(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

CREATE TABLE IF NOT EXISTS `comment_oc` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`comment` varchar(255) NOT NULL,
`comment_date` TIMESTAMP,
`user_id` int(11) NOT NULL,
`report_id` int(11) NOT NULL,
PRIMARY KEY (`id`),
FOREIGN KEY (`user_id`) REFERENCES user_oc(`id`),
FOREIGN KEY (`report_id`) REFERENCES report_oc(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

CREATE TABLE IF NOT EXISTS `cfa_oc` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`cfa_comment` varchar(255) NOT NULL,
`cfa_comment_date` TIMESTAMP,
`user_id` int(11) NOT NULL,
`report_id` int(11) NOT NULL,
PRIMARY KEY (`id`),
FOREIGN KEY (`user_id`) REFERENCES user_oc(`id`),
FOREIGN KEY (`report_id`) REFERENCES report_oc(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;
