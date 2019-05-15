app.service("brandService", function ($http) {
    //查询所有
    this.findAll = function () {
        return $http.get("../brand/findAll.do");
    }
    //分页查询
    this.findpage = function (page, rows) {

        return $http.get("../brand/findPage.do?page=" + page + "&rows=" + rows + "");
    }
    //保存
    this.save = function (uri, entity) {
        return $http.post(uri, entity);
    }
    //查询单个
    this.queryOne = function (id) {
        return $http.get("../brand/findOne.do?id=" + id + "");
    }
    //删除
    this.dele = function (selectIds) {
        return $http.get("../brand/delete.do?ids=" + selectIds)
    }
    //搜索
    this.search = function (page, rows, searchEntity) {
        return $http.post('../brand/search.do?page=' + page + "&rows=" + rows, searchEntity);
    }
})