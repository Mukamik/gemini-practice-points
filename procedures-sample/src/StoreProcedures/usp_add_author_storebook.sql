USE [BookStore]
GO

/****** Object:  StoredProcedure [dbo].[usp_add_author_storebook]    Script Date: 04/25/2020 13:15:32 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO







CREATE PROCEDURE [dbo].[usp_add_author_storebook] @pfirstname varchar(128),
										@psurname varchar(128),
										@psurname2 varchar (128) = null,
										@presultid int out,
										@pmsgerror varchar(256) out
AS
	SET NOCOUNT ON;
	BEGIN TRY
		INSERT INTO Authors(Firstname,Surname,Surname2)
		VALUES(@pfirstname,@psurname,@psurname2);
		IF @@ROWCOUNT > 0 AND @@ERROR = 0
			SELECT @presultid = Id FROM Authors
			WHERE Id = SCOPE_IDENTITY();
	END TRY
	BEGIN CATCH
		SET @pmsgerror =  convert(varchar(8),ERROR_LINE()) + ': ' + ERROR_MESSAGE()
		PRINT 'Line ' + @pmsgerror
	END CATCH







GO

