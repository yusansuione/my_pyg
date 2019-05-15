app.service("loginService", function ($http) {
    this.loginService = function () {
        return $http.get('login/name.do');
    }

})