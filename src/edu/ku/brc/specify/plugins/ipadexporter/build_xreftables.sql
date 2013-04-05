
DROP TABLE IF EXISTS `ios_geoloc`;
DROP TABLE IF EXISTS `ios_colobjagents`;
DROP TABLE IF EXISTS `ios_colobjgeo`;
DROP TABLE IF EXISTS `ios_colobjcnts`;


CREATE TABLE `ios_geoloc` ( `OldID` int(11) NOT NULL DEFAULT '0', `NewID` int(11) NOT NULL DEFAULT '0', PRIMARY KEY (`OldID`), KEY `INX_geoloc` (`NewID`)) 
ENGINE=MYISAM DEFAULT CHARSET=latin1;

CREATE TABLE `ios_colobjagents` ( `OldID` int(11) NOT NULL DEFAULT '0', `NewID` int(11) NOT NULL DEFAULT '0', PRIMARY KEY (`OldID`), KEY `INX_colobjagents` (`NewID`)) 
ENGINE=MYISAM DEFAULT CHARSET=latin1;

CREATE TABLE `ios_colobjgeo` ( `OldID` int(11) NOT NULL DEFAULT '0', `NewID` int(11) NOT NULL DEFAULT '0', PRIMARY KEY (`OldID`), KEY `INX_colobjgeos` (`NewID`)) 
ENGINE=MYISAM DEFAULT CHARSET=latin1;

CREATE TABLE `ios_colobjcnts` ( `OldID` int(11) NOT NULL DEFAULT '0', `NewID` int(11) NOT NULL DEFAULT '0', PRIMARY KEY (`OldID`), KEY `INX_colobjcnts` (`NewID`)) 
ENGINE=MYISAM DEFAULT CHARSET=latin1;
