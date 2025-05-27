use BookStore
declare @presultid int
declare @pmsgerror varchar(256)
set @presultid = null
set @pmsgerror = null
EXEC dbo.usp_add_author_storebook 'Emily'
									,'Vander'
									,'Veer'
									,@presultid output
									,@pmsgerror output
if @presultid = null
print @pmsgerror
else
print 'Id ' + convert(varchar(2),@presultid) + ' added'