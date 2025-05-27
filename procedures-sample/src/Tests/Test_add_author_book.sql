use BookStore
DECLARE @presult bit
declare @pmsgerror varchar(256)
EXEC dbo.usp_add_authorbook_storebook 1,'0764576593',@presult output,@pmsgerror output
IF @presult = 0
	print @pmsgerror
else
print 'Added'