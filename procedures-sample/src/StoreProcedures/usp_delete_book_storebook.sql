USE [BookStore]
GO

/****** Object:  StoredProcedure [dbo].[usp_delete_book_storebook]    Script Date: 04/25/2020 13:16:27 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO


CREATE PROCEDURE [dbo].[usp_delete_book_storebook] @pisbn varchar(13),
										@presult bit out,
										@pmsgerror varchar(256) out
AS
	SET NOCOUNT ON;
	BEGIN TRY
		BEGIN TRAN
		DELETE FROM AuthorBook WHERE Isbn = @pisbn
		--DELETE AUTHORS ASSOCIATED TO THIS BOOK
		IF @@ROWCOUNT > 0 AND @@ERROR = 0
		BEGIN
				DELETE FROM Books
				WHERE
				Isbn = @pisbn
				IF @@ERROR = 0 AND @@ROWCOUNT = 1
					set @presult = 1
				ELSE
					SET @presult = 0
		END
		ELSE
			SET @pmsgerror = 'Nothing to delete'
		IF @presult = 1
			COMMIT TRAN
		ELSE
			ROLLBACK TRAN
	END TRY
	BEGIN CATCH
		SET @pmsgerror = convert(varchar(8),ERROR_LINE()) + ': ' + ERROR_MESSAGE()
		PRINT 'Line ' + @pmsgerror
	END CATCH







GO

