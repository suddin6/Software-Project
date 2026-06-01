-- create database
DROP DATABASE IF EXISTS voting_veranda;
CREATE DATABASE voting_veranda;

-- select database
USE voting_veranda;

CREATE TABLE user_type
(
    user_id		INT			AUTO_INCREMENT  NOT NULL,
    user_type		VARCHAR(10)			NOT NULL,
    CONSTRAINT user_pk
		PRIMARY KEY (user_id)
);

-- create tables
CREATE TABLE login
(
	login_id		INT			AUTO_INCREMENT NOT NULL,
    l_username	VARCHAR(50)	NOT NULL,
    l_password VARCHAR(255)	NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    user_id INT 	NOT NULL,
	CONSTRAINT	login_pk
		PRIMARY KEY (login_id),
	CONSTRAINT login_user_fk
		FOREIGN KEY (user_id) REFERENCES user_type (user_id)
);

CREATE TABLE voter
(
	voter_id		INT			AUTO_INCREMENT NOT NULL,
    ssn		VARCHAR(15)			UNIQUE NOT NULL,
    vote_status		BOOLEAN	NOT NULL,
    login_id		INT	NOT NULL,
    CONSTRAINT voter_pk
		PRIMARY KEY (voter_id),
	CONSTRAINT voter_login_fk
		FOREIGN KEY (login_id) REFERENCES login (login_id)
);

CREATE TABLE positions
(
	position_id			INT			AUTO_INCREMENT NOT NULL,
    position_name		VARCHAR(50)	NOT NULL,
    CONSTRAINT positions_pk
		PRIMARY KEY (position_id)
);

CREATE TABLE candidate
(
	candidate_id		INT			AUTO_INCREMENT NOT NULL,
    party		VARCHAR(50)			NOT NULL,
    campaign 	VARCHAR(1000) NOT NULL,
    login_id	INT 		NOT NULL,
    position_id INT 		NOT NULL,
    CONSTRAINT candidate_pk
		PRIMARY KEY (candidate_id),
    CONSTRAINT candidate_login_fk
		FOREIGN KEY (login_id) REFERENCES login (login_id),
	CONSTRAINT candidate_position_fk
		FOREIGN KEY (position_id) REFERENCES positions (position_id)
);

CREATE TABLE admins
(
	admin_id		INT			NOT NULL,
    login_id			INT			NOT NULL,
    CONSTRAINT admin_pk
		PRIMARY KEY (admin_id),
	CONSTRAINT admin_login_fk
		FOREIGN KEY (login_id) REFERENCES login (login_id)
);

CREATE TABLE votes
(
	vote_id			INT			AUTO_INCREMENT NOT NULL,
    voter_id		INT		 	NOT NULL,
    candidate_id	INT			NOT NULL,
    CONSTRAINT	votes_pk
		PRIMARY KEY (vote_id),
	CONSTRAINT votes_voter_fk
		FOREIGN KEY (voter_id) REFERENCES voter (voter_id),
	CONSTRAINT votes_candidate_fk
		FOREIGN KEY (candidate_id) REFERENCES candidate (candidate_id)
);

-- insert rows into tables
INSERT INTO user_type VALUES
(1, 'Voter'),
(2, 'Candidate'),
(3, 'Admin');

INSERT INTO positions VALUES
(1, 'President'),
(2, 'Senator');

INSERT INTO login VALUES			
(1, 'suddin6', 'randomPass@1', 'Sumaya', 'Uddin', 1),			
(2, 'atoma2', 'randomPass@2', 'Athraa', 'Toma', 1),			
(3, 'jmsopoci', 'randomPass@3', 'John', 'Sopoci', 1),			
(4, 'ssinha', 'randomPass@4', 'Suhani', 'Sinha', 1),			
(5, 'strong5', 'randomPass@5', 'Adil', 'Strong', 3),			
(6, 'criddle', 'randomPass@6', 'Casper', 'Riddle', 1),			
(7, 'dorajo3', 'randomPass@7', 'Dora', 'Jo', 2),			
(8, '8gsmith', 'randomPass@8', 'Gregory', 'Smith', 2),			
(9, 'sacciaio', 'randomPass@9', 'Sara', 'Acciaio', 2),			
(10, 'charfan', 'randomPass@10', 'Charolette', 'Fan', 3),			
(11, 'lmullins7', 'randomPass@11', 'Lisa', 'Mullins', 2),			
(12, 'hermanm', 'randomPass@12', 'Micheal', 'Herman', 2),			
(13, '25tknowles', 'randomPass@13', 'Tony', 'Knowles', 2),		
(14, 'rpalmer', 'randomPass@14', 'Rosalie', 'Palmer', 1),			
(15, 'chasej', 'randomPass@15', 'Jim', 'Chase', 1);

INSERT INTO voter VALUES	
(1, '123-456-7890', 1, 1),	
(2, '123-456-7891', 1, 2),	
(3, '123-456-7892', 0, 3),	
(4, '123-456-7893', 0, 4),	
(5, '123-456-7894', 0, 6),	
(6, '123-456-7895', 1, 14),	
(7, '123-456-7896', 1, 15);	

INSERT INTO candidate VALUES			
(1, 'Democrat', '"We are the Democratic Party. We''re rolling up our sleeves and organizing for a brighter, more equal future. Together, we will elect Democrats up and down the ballot." - democrats.org', 7, 1),			
(2, 'Republican', '"[O]ur Party''s history is filled with the stories of brave men and women who gave everything they had to build America into the greatest nation in the history of the world." - rnc.org', 8, 1),			
(3, 'Green Party', '"We are grassroots activists, environmentalists, advocates for social justice, nonviolent resisters and regular citizens who''ve had enough of corporate-dominated politics. Government must be part of the solution, but when it''s controlled by the 1%, it''s part of the problem. The longer we wait for change, the harder it gets. Don''t stay home on election day. Vote Green!" - gp.org', 9, 1),			
(4, 'Democrat', '"All across the country, from California to Texas to Georgia, Democrats have overperformed — in 265 out of 296 key elections. We will keep delivering a powerful message: Democrats aren''t bowing to Trump and his allies. We''re standing up. We''re fighting back." - democracy.org', 11, 2),			
(5, 'Republican', '"America needs determined Republican leadership at every level of government to address the core threats to our very survival: the growing aggression from China; the invasion of illegal aliens facilitated by the previous administration; and the relentless attacks on our system of justice, which has been weaponized to target political opponents and erode the very foundations of our democracy." - rnc.org', 12, 2),			
(6, 'Green Party', '"The Green Party of the United States is a grassroots national party. We''re the party for "We The People," the health of our planet, and future generations instead of the One Percent. We welcome all those who refuse to accept a choice limited to the Two Parties of War and Wall Street." - gp.org', 13, 2);			

INSERT INTO admins VALUES
(1, 5),
(2, 10);

INSERT INTO votes VALUES
(1, 1, 1),
(2, 2, 1),
(3, 3, 2),
(4, 4, 6),
(5, 5, 6),
(6, 6, 6),
(7, 7, 5),
(8, 1, 4),
(9, 2, 4),
(10, 3, 6),
(11, 4, 2),
(12, 5, 1),
(13, 6, 2),
(14, 7, 3);