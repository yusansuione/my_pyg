//定义控制器
app.controller("brandController", function ($scope, $controller, brandService) {
    //绑定父控制器
    $controller('baseController', {$scope: $scope})
    //查询所有产品列表
    $scope.queryAll = function () {
        brandService.queryAll.success(function (data) {
            $scope.list = data;
        })
    }


    //分页查询
    $scope.findpage = function (page, rows) {
        brandService.findpage(page, rows).success(function (data) {
            $scope.list = data.rows;
            //重新设置查询总数
            $scope.paginationConf.totalItems = data.total;
        })
    }

    //添加
    $scope.save = function () {
        var uri = "../brand/add.do";
        if ($scope.entity.id != null) {
            uri = "../brand/update.do"
        }
        brandService.save(uri, $scope.entity).success(function (data) {
            //添加成功
            if (data.success) {
                $scope.reloadList();
            } else {
                alert(data.message)
            }
        })
    }
    //查询一个
    $scope.queryOne = function (id) {
        brandService.queryOne(id).success(function (data) {
            $scope.entity = data;
        })
    }


    //删除
    $scope.dele = function () {
        brandService.dele($scope.selectIds).success(function (data) {
            if (data.success) {
                $scope.reloadList();
            } else {
                alert(data.message)
            }
        })
    }

    //定义搜索的对象
    $scope.searchEntity = {};

    $scope.search = function (page, rows) {
        brandService.search(page, rows, $scope.searchEntity).success(function (data) {
            $scope.list = data.rows;
            $scope.paginationConf.totalItems = data.total;
        })
    }

})