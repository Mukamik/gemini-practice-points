USE [BookStore]
GO

/****** Object:  StoredProcedure [dbo].[usp_add_book_storebook]    Script Date: 04/25/2020 13:15:58 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO







CREATE PROCEDURE [dbo].[usp_add_book_storebook] @pisbn varchar(13),
										@ptitle varchar(256),
										@ppages int = null,
										@pyear int = null,
										@pcategory int = null,
										@presult varchar(13) out,
										@pmsgerror varchar(256) out
AS
	SET NOCOUNT ON;
	BEGIN TRY
		INSERT INTO Books(Isbn,Title,Pages,[Year],CategoryId)
		VALUES(@pisbn,@ptitle,@ppages,@pyear,@pcategory);
		IF @@ROWCOUNT > 0 AND @@ERROR = 0
			SELECT @presult = Isbn  FROM Books
			WHERE Isbn = @pisbn;
	END TRY
	BEGIN CATCH
		SET @pmsgerror = convert(varchar(8),ERROR_LINE()) + ': ' + ERROR_MESSAGE()
		PRINT 'Line ' + @pmsgerror
	END CATCH







GO

