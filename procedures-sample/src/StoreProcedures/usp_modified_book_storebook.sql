USE [BookStore]
GO

/****** Object:  StoredProcedure [dbo].[usp_modified_book_storebook]    Script Date: 04/25/2020 13:17:26 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO




CREATE PROCEDURE [dbo].[usp_modified_book_storebook] @pisbn varchar(13),
										@ptitle varchar(256),
										@ppages int = null,
										@pyear int = null,
										@pcategory int = null,
										@presult bit out,
										@pmsgerror varchar(256) out
AS
	SET NOCOUNT ON;
	BEGIN TRY
		UPDATE Books
		SET
		Title = @ptitle,
		Pages = @ppages,
		[Year] = @ppages,
		CategoryId = @pcategory
		WHERE
		Isbn = @pisbn
		
		IF @@ROWCOUNT > 0 AND @@ERROR = 0
			set @presult = 1
	END TRY
	BEGIN CATCH
		SET @pmsgerror = convert(varchar(8),ERROR_LINE()) + ': ' + ERROR_MESSAGE()
		PRINT 'Line ' + @pmsgerror
	END CATCH




GO

