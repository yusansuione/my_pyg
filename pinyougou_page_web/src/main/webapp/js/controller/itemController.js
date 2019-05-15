app.controller('itemController', function ($scope, $http) {
    /**
     * 数量操作
     * @param x
     */
    $scope.Num = 1;
    $scope.addNum = function (x) {
        $scope.Num = x;
        if ($scope.Num < 1) {
            $scope.Num = 1;
        }

    }


    $scope.specificationItems = {};//记录用户选择的规格
    //用户选择规格
    $scope.selectSpecification = function (name, value) {
        $scope.specificationItems[name] = value;

        searchSku();//读取sku
    }

    //加载sku
    $scope.loadSku = function () {
        $scope.sku = skuList[0];
        $scope.specificationItems = JSON.parse(JSON.stringify($scope.sku.spec))
    }

    //匹配两个对象
    matchObject = function (map1, map2) {
        for (var k in map1) {
            if (map1[k] != map2[k]) {
                return false;
            }
        }
        for (var k in map2) {
            if (map2[k] != map1[k]) {
                return false;
            }
        }
        return true;
    }

    //查询SKU
    searchSku = function () {
        for (var i = 0; i < skuList.length; i++) {
            if (matchObject(skuList[i].spec, $scope.specificationItems)) {
                $scope.sku = skuList[i];
                return;
            }
        }
        $scope.sku = {id: 0, title: '--------', price: 0};//如果没有匹配的
    }


    //添加商品到购物车
    $scope.addToCart = function () {
        $http.get("http://localhost:8089/cart/saveCartList.do?itemId="
            + $scope.sku.id + "&num=" + $scope.Num, {'withCredentials': true}).success(function (responces) {
            if (responces.success) {
                window.location.href = 'http://localhost:8089/cart.html';
            } else {
                alert(responces.message);
            }
        })
    }


})