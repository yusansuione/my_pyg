app.controller("searchController", function ($scope, $location, searchService) {
    /**
     * 搜索
     */

    /**
     * 搜索对象
     * @type {{keywords: 关键字, category: 商品分类, brand: 品牌, spec: {'网络'：'移动4G','机身内存':'64G'}},price:价格,,'pageNo':'当前页数','pageSize':'每页多少条'}
     */
    $scope.searchMap = {
        'keywords': '',
        'category': '',
        'brand': '',
        'spec': {},
        'price': '',
        'pageNo': '1',
        'pageSize': '20',
        'filedsort': '',
        'sort': ''
    };

    $scope.search = function () {
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(function (responces) {
            //搜索返回结果
            $scope.resultMap = responces;
            //分页
            buildPageLabel();
        })
    }

    /**
     * 添加搜索条件
     * @param key
     * @param value
     */
    $scope.addSearchItem = function (key, value) {
        if (key == "brand" || key == "category" || key == "price") {
            $scope.searchMap[key] = value;
        } else {
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();
    }

    /**
     * 添加搜索条件
     * @param key
     * @param value
     */
    $scope.removeSearchItem = function (key, value) {
        if (key == "brand" || key == "category" || key == "price") {
            $scope.searchMap[key] = "";
        } else {
            delete $scope.searchMap.spec[key];
        }
        $scope.search();
    }

    buildPageLabel = function () {
        $scope.pageLabel = []; //定义分页栏
        var firstPage = 1;
        var lastPage = $scope.resultMap.totalpages;

        $scope.firstDot = true;//前面有点
        $scope.lastDot = true;//后边有点

        if ($scope.resultMap.totalpages > 5) {

            if ($scope.searchMap.pageNo < 3) {
                lastPage = 5;
                $scope.firstDot = false;//前面没点
            } else if ($scope.resultMap.totalpages - ($scope.resultMap.pageNo) > 2) {
                firstPage = $scope.resultMap.totalpages - 4;
                $scope.lastDot = false;//后边有点
            } else {

                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;

            }
        } else {
            $scope.firstDot = false;//前面有点
            $scope.lastDot = false;//后边有点
        }
        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageLabel.push(i);
        }
    }

    /**
     * 点击事件,根据点击的条件跳转页数
     * @param pageNo
     */
    $scope.queryByPage = function (pageNo) {

        if (pageNo < 1 || pageNo > $scope.resultMap.totalpages) {
            alert("请输入正确的页码")
            return;
        }
        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    }


    //设置排序规则
    $scope.sortSearch = function (filedsort, sort) {
        $scope.searchMap.filedsort = filedsort;
        $scope.searchMap.sort = sort;

        $scope.search();
    }


    /**
     *判断关键字是不是品牌
     * @returns {boolean}
     */
    $scope.keywordsIsBrand = function () {
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 加载查询字符串
     */
    $scope.loadkeywords = function () {
        $scope.searchMap.keywords = $location.search()['keywords'];
        $scope.search();
    }
})