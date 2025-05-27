USE [BookStore]
GO

/****** Object:  StoredProcedure [dbo].[usp_get_authors_storebook]    Script Date: 04/25/2020 13:16:37 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO








CREATE PROCEDURE [dbo].[usp_get_authors_storebook] 
@pfirstname varchar(128) = '%'
,@pSurname varchar(128) = '%'
AS
	SET NOCOUNT ON;
	SELECT * FROM
	Authors
	WHERE Firstname LIKE @pfirstname AND
	Surname = @pSurname


GO

