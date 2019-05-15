//控制层
app.controller('goodsController', function ($scope, $controller, $location, goodsService, uploadService, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function () {

        //根据angularjs 的路由传参

        var id = $location.search()['id'];
        if (id == null) {
            return;
        }
        goodsService.findOne(id).success(
            function (response) {
                alert(JSON.stringify(response))
                $scope.entity = response;
                //显示富文本框
                editor.html($scope.entity.goodsDesc.introduction)
                //显示图片列表
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);

                //显示扩展属性
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);

                //规格列表
                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);

                //sku列表
                for (var i = 0; i < $scope.entity.itemList.length; i++) {
                    $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
                }
            }
        );
    }

    //定义一个判断，复选框回显的选中状态
    $scope.checkAttributeValue = function (specName, optionName) {
        var item = $scope.entity.goodsDesc.specificationItems;
        var spec = $scope.searchObjectByKey(item, "attributeName", specName);
        if (spec == null) {
            return false;
        } else {
            if (spec.attributeValue.indexOf(optionName) >= 0) {
                return true;
            } else {
                return false
            }
        }
        return true;
    }

    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    $scope.reloadList();//重新加载
                } else {
                    alert(response.message);
                }
            }
        );
    }

    //保存商品
    $scope.add = function () {
        $scope.entity.goodsDesc.introduction = editor.html();

        if ($scope.entity.goods.id == null) {
            var add = goodsService.add($scope.entity);
        } else {
            var add = goodsService.update($scope.entity);
        }
        add.success(
            function (response) {
                alert(response.message);
                if (response.success) {
                    location.href = "goods.html";//跳到商品展示列表
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }
    //上传图片
    $scope.uploadFile = function () {
        uploadService.uploadService().success(function (response) {
            //如果上传成功则绑定到地址上
            if (response.success) {
                $scope.image_entity.url = response.message;
            } else {
                alert(response.message)
            }
        }).error(function () {
            alert("文件上传错误")
        })
    }

    $scope.entity = {goods: {}, goodsDesc: {itemImages: [], specificationItems: []}, itemList: []};//定义图片实体类

    //添加图片
    $scope.add_image_entity = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity)
    }
    $scope.dele_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1)
    }

    //一级分类,下拉框
    $scope.selectItemCat1List = function () {
        itemCatService.findByParentId(0).success(function (response) {
            $scope.itemCat1List = response;
        })
    }

    //二级分类,下拉框
    $scope.$watch("entity.goods.category1Id", function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.itemCat2List = response;
            $scope.entity.goods.category2Id = -1;
        })
    })
    //三级分类,下拉框
    $scope.$watch("entity.goods.category2Id", function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.itemCat3List = response;
        })
    })

    //查询模板ID
    $scope.$watch("entity.goods.category3Id", function (newValue, odlValue) {
        itemCatService.findOne(newValue).success(function (response) {
            $scope.entity.goods.typeTemplateId = response.typeId;
        })
    })

    //根据模板id查询商品分类
    $scope.$watch("entity.goods.typeTemplateId", function (newValue, oldValue) {
        typeTemplateService.findOne(newValue).success(function (response) {
            $scope.typeTemplate = response;
            $scope.typeTemplate.brandIds = JSON.parse(response.brandIds);
            //扩展属性
            if ($location.search()['id'] = null) {
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse(response.customAttributeItems);
            }
            //根据模板id查询规格列表
            typeTemplateService.findSpecList(newValue).success(function (response) {
                // alert(JSON.stringify(response))
                $scope.specList = response;
            })
        })
    })


    /**
     * 页面规格checkbox的点击事件
     * @param $event 整个checkbox本身
     * @param specName 规格名称
     * @param optionName 选项名称
     */
    $scope.updateSpecAttribute = function ($event, specName, optionName) {
        //查找规格有没有保存过
        var obj = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, 'attributeName', specName);
        //找到相关记录
        if (obj == null) {
            //添加一条记录
            $scope.entity.goodsDesc.specificationItems.push(
                {
                    "attributeName": specName,
                    "attributeValue": [
                        optionName
                    ]
                }
            )
        } else {
            if ($event.target.checked) {
                //如果已选中则追加一条记录
                obj.attributeValue.push(optionName);
            } else {
                //查找当前value的下标
                var optionIndex = obj.attributeValue.indexOf(optionName);
                //删除数据
                obj.attributeValue.splice(optionIndex, 1);
                //取消勾选后，如果当前列表里没有记录时，删除当前整个规格
                if (obj.attributeValue.length < 1) {
                    var number = $scope.entity.goodsDesc.specificationItems.indexOf(obj);
                    $scope.entity.goodsDesc.specificationItems.splice(number, 1)
                }
            }
        }
    }

    // 1. 	创建$scope.createItemList方法，同时创建一条有基本数据，不带规格的初始数据
    $scope.createItemList = function () {
        // 参考: $scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0' }]
        $scope.entity.itemList = [{spec: {}, price: 0, num: 99999, status: '0', isDefault: '0'}];
        // 2. 	查找遍历所有已选择的规格列表，后续会重复使用它，所以我们可以抽取出个变量items
        var items = $scope.entity.goodsDesc.specificationItems;
        for (var i = 0; i < items.length; i++) {


            // 9. 	回到createItemList方法中，在循环中调用addColumn方法，并让itemList重新指向返回结果;
            $scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
        }
    }


    // 3. 	抽取addColumn(当前的表格，列名称，列的值列表)方法，用于每次循环时追加列
    var addColumn = function (itemList, attributeName, attributeValue) {
        // 4. 	编写addColumn逻辑，当前方法要返回添加所有列后的表格，定义新表格变量newList
        var newList = [];
        // 5. 	在addColumn添加两重嵌套循环，一重遍历之前表格的列表，二重遍历新列值列表
        for (var i = 0; i < itemList.length; i++) {
            for (var j = 0; j < attributeValue.length; j++) {
                // 6. 	在第二重循环中，使用深克隆技巧，把之前表格的一行记录copy所有属性，用到var newRow = JSON.parse(JSON.stringify(之前表格的一行记录));
                var newRow = JSON.parse(JSON.stringify(itemList[i]));
                // 7. 	接着第6步，向newRow里追加一列
                newRow.spec[attributeName] = attributeValue[j];
                // 8. 	把新生成的行记录，push到newList中
                newList.push(newRow);
            }
        }

        return newList;

    };

    $scope.status = ['未审核', '已审核', '审核未通过', '关闭'];//商品状态

    $scope.itemCatList = [];//商品分类列表
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(function (response) {
            for (var i = 0; i < response.length; i++) {
                $scope.itemCatList[response[i].id] = response[i].name;
            }
        })
    }


});
