-- drop table phone_book;

create table phone_book (
	id int PRIMARY KEY NOT NULL auto_increment,
	name varchar(80) not null,
	mobile varchar(20) null
);
