USE [BookStore]
GO

/****** Object:  StoredProcedure [dbo].[usp_add_category_storebook]    Script Date: 04/25/2020 13:16:08 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO







CREATE PROCEDURE [dbo].[usp_add_category_storebook] @pcategory varchar(46),
										@presultid int out,
										@pmsgerror varchar(256) out
AS
	SET NOCOUNT ON;
	SET @presultid = NULL
	BEGIN TRY
		INSERT INTO Categories(Category)
		VALUES(@pcategory);
		IF @@ROWCOUNT > 0 AND @@ERROR = 0
			SELECT @presultid = Id FROM Categories
			WHERE Id = SCOPE_IDENTITY();
	END TRY
	BEGIN CATCH
		SET @pmsgerror = convert(varchar(8),ERROR_LINE()) + ': ' + ERROR_MESSAGE()
		PRINT 'Line ' + @pmsgerror
	END CATCH










GO

