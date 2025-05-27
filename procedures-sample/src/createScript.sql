USE BookStore
;

IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = object_id('FK_AuthorBook_Authors') AND OBJECTPROPERTY(id, 'IsForeignKey') = 1)
ALTER TABLE AuthorBook DROP CONSTRAINT FK_AuthorBook_Authors
;

IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = object_id('FK_AuthorBook_Books') AND OBJECTPROPERTY(id, 'IsForeignKey') = 1)
ALTER TABLE AuthorBook DROP CONSTRAINT FK_AuthorBook_Books
;

IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = object_id('FK_Books_Categories') AND OBJECTPROPERTY(id, 'IsForeignKey') = 1)
ALTER TABLE Books DROP CONSTRAINT FK_Books_Categories
;



IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = object_id('AuthorBook') AND  OBJECTPROPERTY(id, 'IsUserTable') = 1)
DROP TABLE AuthorBook
;

IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = object_id('Authors') AND  OBJECTPROPERTY(id, 'IsUserTable') = 1)
DROP TABLE Authors
;

IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = object_id('Books') AND  OBJECTPROPERTY(id, 'IsUserTable') = 1)
DROP TABLE Books
;

IF EXISTS (SELECT * FROM dbo.sysobjects WHERE id = object_id('Categories') AND  OBJECTPROPERTY(id, 'IsUserTable') = 1)
DROP TABLE Categories
;


CREATE TABLE AuthorBook ( 
	IdAuthor int NOT NULL,
	Isbn varchar(13) NOT NULL,
	Created datetime DEFAULT getdate() NOT NULL
)
;

CREATE TABLE Authors ( 
	Id int identity(1,1)  NOT NULL,
	Firstname varchar(128) NOT NULL,
	Surname varchar(128) NOT NULL,
	Surname2 varchar(128)
)
;

CREATE TABLE Books ( 
	Isbn varchar(13) NOT NULL,
	Title varchar(256) NOT NULL,
	Pages int,
	Year int,
	CategoryId int
)
;

CREATE TABLE Categories ( 
	Id int identity(1,1)  NOT NULL,
	Category varchar(64) NOT NULL
)
;


ALTER TABLE Categories
	ADD CONSTRAINT UQ_Categories_Category UNIQUE (Category)
;

ALTER TABLE Authors ADD CONSTRAINT PK_Authors 
	PRIMARY KEY CLUSTERED (Id)
;

ALTER TABLE Books ADD CONSTRAINT PK_Books 
	PRIMARY KEY CLUSTERED (Isbn)
;

ALTER TABLE Categories ADD CONSTRAINT PK_Categories 
	PRIMARY KEY CLUSTERED (Id)
;



ALTER TABLE AuthorBook ADD CONSTRAINT FK_AuthorBook_Authors 
	FOREIGN KEY (IdAuthor) REFERENCES Authors (Id)
	ON DELETE CASCADE ON UPDATE CASCADE
;

ALTER TABLE AuthorBook ADD CONSTRAINT FK_AuthorBook_Books 
	FOREIGN KEY (Isbn) REFERENCES Books (Isbn)
	ON DELETE NO ACTION ON UPDATE NO ACTION
;

ALTER TABLE Books ADD CONSTRAINT FK_Books_Categories 
	FOREIGN KEY (CategoryId) REFERENCES Categories (Id)
	ON DELETE SET NULL ON UPDATE CASCADE
;
