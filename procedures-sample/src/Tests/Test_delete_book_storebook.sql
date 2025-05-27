declare @presult bit = 0
declare @pmsgerror varchar(256) = null
EXEC dbo.usp_delete_book_storebook '0764576593'
					,@presult output
					,@pmsgerror output
if @presult = 0
print @pmsgerror
else
print 'Id ' + convert(varchar(13),@presult) + ' deleted'