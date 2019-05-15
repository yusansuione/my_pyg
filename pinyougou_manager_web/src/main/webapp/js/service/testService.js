//服务层
app.service('testService', function ($http) {

    //读取列表数据绑定到表单中
    this.findAll = function () {
        return $http.get('../test/findAll.do');
    }
    //分页
    this.findPage = function (page, rows) {
        return $http.get('../test/findPage.do?page=' + page + '&rows=' + rows);
    }
    //查询实体
    this.findOne = function (id) {
        return $http.get('../test/findOne.do?id=' + id);
    }
    //增加
    this.add = function (entity) {
        return $http.post('../test/add.do', entity);
    }
    //修改
    this.update = function (entity) {
        return $http.post('../test/update.do', entity);
    }
    //删除
    this.dele = function (ids) {
        return $http.get('../test/delete.do?ids=' + ids);
    }
    //搜索
    this.search = function (page, rows, searchEntity) {
        return $http.post('../test/search.do?page=' + page + "&rows=" + rows, searchEntity);
    }
});
