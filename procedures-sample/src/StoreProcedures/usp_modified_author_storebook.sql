USE [BookStore]
GO

/****** Object:  StoredProcedure [dbo].[usp_modified_author_storebook]    Script Date: 04/25/2020 13:16:58 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO






CREATE PROCEDURE [dbo].[usp_modified_author_storebook] @pfirstname varchar(128),
										@psurname varchar(128),
										@psurname2 varchar (128) = null,
										@pid int,
										@presult bit out,
										@pmsgerror varchar(256) out
AS
	SET NOCOUNT ON;
	BEGIN TRY
	UPDATE Authors
	SET Firstname = @pfirstname,
	Surname = @psurname,
	Surname2 = @psurname2
	WHERE Id = @pid 
		IF @@ROWCOUNT > 0 AND @@ERROR = 0
			set @presult = 1
	END TRY
	BEGIN CATCH
		SET @pmsgerror = CONVERT(varchar(8),ERROR_NUMBER()) + ': ' + ERROR_MESSAGE()
		PRINT 'Line ' + @pmsgerror
	END CATCH






GO

