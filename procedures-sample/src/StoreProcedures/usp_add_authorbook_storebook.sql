USE [BookStore]
GO

/****** Object:  StoredProcedure [dbo].[usp_add_authorbook_storebook]    Script Date: 04/25/2020 13:15:48 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO










CREATE PROCEDURE [dbo].[usp_add_authorbook_storebook] @pauthorid int,
										@pisbn varchar(13),
										@presult bit out,
										@pmsgerror varchar(256) out
AS
	SET NOCOUNT ON;
	BEGIN TRY
		INSERT INTO AuthorBook(IdAuthor,Isbn,Created)
		VALUES(@pauthorid,@pisbn,GETDATE());
		IF @@ROWCOUNT > 0 AND @@ERROR = 0
			SET @presult = 1
	END TRY
	BEGIN CATCH
		SET @pmsgerror =  convert(varchar(8),ERROR_LINE()) + ': ' + ERROR_MESSAGE()
		PRINT 'Line ' + @pmsgerror
	END CATCH










GO

