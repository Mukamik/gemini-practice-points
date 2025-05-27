declare @presult varchar(13)
declare @pmsgerror varchar(256)
set @presult = null
set @pmsgerror = null
EXEC dbo.usp_modified_book_storebook '0764576593'
					,'JavaScript for Dummies'
					,387
					,2005
					,1
					,@presult output
					,@pmsgerror output
if @presult = null
print @pmsgerror
else
print 'Id ' + convert(varchar(13),@presult) + ' modified'