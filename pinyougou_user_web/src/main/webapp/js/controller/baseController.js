app.controller("baseController", function ($scope) {
//分页函数
    $scope.paginationConf = {
        //当前页
        currentPage: 1,
        //总记录数
        totalItems: 10,
        //每页查询的记录数
        itemsPerPage: 10,
        //分页选项，用于选择每页显示多少条记录
        perPageOptions: [10, 20, 30, 40, 50],
        //当页码变更后触发的函数
        onChange: function () {
            $scope.reloadList();//重新加载
        }
    };

    $scope.reloadList = function () {

        // $scope.findpage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage)
    }

//选中删除的id
    $scope.selectIds = [];

    $scope.deleteSelect = function ($event, id) {
        if ($event.target.checked) {
            $scope.selectIds.push(id)
        } else {
            //查询当前id的数组下标
            var idx = $scope.selectIds.indexOf(id);
            //根据下标删除元素
            $scope.selectIds.splice(idx, 1)
        }

    }

    //跟据需求输出json串
//jsonString要转换的json串,key要读取的值
    $scope.jsonToString = function (jsonString, key) {
        var json = JSON.parse(jsonString);
        var result = "";
        for (var i = 0; i < json.length; i++) {
            if (i > 0) {
                result += ",";
            }
            result += json[i][key];
        }
        return result;
    }


    /**
     * 搜索一个数组中某个属性是否等于某个值
     * @param list 数组
     * @param key 查找的属性名
     * @param value 对比的值
     * @return 查找到的结果，null,代表查找不到
     */
    $scope.searchObjectByKey = function (list, key, value) {
        for (var i = 0; i < list.length; i++) {
            if (list[i][key] == value) {
                return list[i]
            }
        }
        return null
    }

})