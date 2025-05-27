USE [BookStore]
GO

/****** Object:  StoredProcedure [dbo].[usp_get_books_storebook]    Script Date: 04/25/2020 13:16:45 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO








CREATE PROCEDURE [dbo].[usp_get_books_storebook]
@pisbn varchar(13) = '%'
,@ptitle varchar(256) = '%'
AS
	SET NOCOUNT ON;
	SELECT 
	b.Isbn
	,b.Title
	,b.Pages
	,b.[Year]
	,cat.Category
	,a.Surname + ', ' + a.Firstname 'Author'
	FROM 
	Books b
	JOIN
	Categories cat ON b.CategoryId = cat.Id
	JOIN
	AuthorBook ab ON b.Isbn = ab.Isbn
	JOIN
	Authors a ON a.Id = ab.IdAuthor
	WHERE b.Isbn LIKE @pisbn
	AND b.Title LIKE @ptitle




GO

