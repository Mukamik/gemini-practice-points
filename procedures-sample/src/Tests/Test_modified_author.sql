declare @presultid int
declare @pmsgerror varchar(256)
set @presultid = null
set @pmsgerror = null
EXEC dbo.usp_modified_author_storebook 'Steven'
									,'W.'
									,'Disbrow'
									,2
									,@presultid output
									,@pmsgerror output
if @presultid = null
print @pmsgerror
else
print 'Id ' + convert(varchar(2),@presultid) + ' modified'