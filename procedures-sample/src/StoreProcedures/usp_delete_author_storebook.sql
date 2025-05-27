USE [BookStore]
GO

/****** Object:  StoredProcedure [dbo].[usp_delete_author_storebook]    Script Date: 04/25/2020 13:16:18 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO






CREATE PROCEDURE [dbo].[usp_delete_author_storebook] @pid int,
										@presult bit out,
										@pmsgerror varchar(256) out
AS
	SET NOCOUNT ON;
	BEGIN TRY
	DELETE FROM Authors
	WHERE Id = @pid 
		IF @@ROWCOUNT > 0 AND @@ERROR = 0
			set @presult = 1
	END TRY
	BEGIN CATCH
		SET @pmsgerror = convert(varchar(8),ERROR_LINE()) + ': ' + ERROR_MESSAGE()
		PRINT 'Line ' + @pmsgerror
	END CATCH






GO

