USE [BookStore]
GO

/****** Object:  StoredProcedure [dbo].[usp_modified_authorbook_storebook]    Script Date: 04/25/2020 13:17:16 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO






CREATE PROCEDURE [dbo].[usp_modified_authorbook_storebook] @pauthorid int,
										@pisbn varchar(13),
										@presult bit out,
										@pmsgerror varchar(256) out
AS
	SET NOCOUNT ON;
	BEGIN TRY
	UPDATE AuthorBook
	SET IdAuthor = @pauthorid,
	Isbn = @pisbn
	WHERE IdAuthor = @pauthorid 
		IF @@ROWCOUNT > 0 AND @@ERROR = 0
			set @presult = 1
	END TRY
	BEGIN CATCH
		SET @pmsgerror = CONVERT(varchar(8),ERROR_NUMBER()) + ': ' + ERROR_MESSAGE()
		PRINT 'Line ' + @pmsgerror
	END CATCH






GO

