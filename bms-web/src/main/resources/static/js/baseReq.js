const http = axios.create({
    baseURL: BD.localhostPath, // url = base url + request url
    withCredentials: true,
    // headers:{'Content-Type': 'application/json;charset=UTF-8'}
    // request timeout
    // timeout: 15000
});


// 添加一个响应拦截器
http.interceptors.response.use((response) => {
    const vm = new Vue();
    const {rtnFlag, rtnMsg} = response.data;
    if (rtnFlag === "1001") {
        vm.$alert('您已登录超时,请重新登录', 'WARNING', {
            confirmButtonText: '确定',
            showClose: false,
            callback: () => {
                window.parent.location.href = BD.localhostPath + '/login.html';
            }
        });
        return false;
    } else if (rtnFlag && rtnFlag === 'shiro') {
        vm.$message({
            type: "error",
            message: "当前未授权进行此操作!"
        });
        return false;
    } else if (rtnFlag && rtnFlag !== '9999') {
        vm.$message({
            type: "error",
            message: rtnMsg || "系统异常,请联系管理员"
        });
        return false;
    } else if (response.headers['content-type'] === "application/vnd.ms-excel;charset=utf-8") {
        let filename = response.headers['content-disposition'];
        filename = filename.replace('attachment;filename=', '');
        let blob = new Blob([response.data]);
        let downloadElement = document.createElement('a');
        let href = window.URL.createObjectURL(blob); //创建下载的链接
        downloadElement.href = href;
        downloadElement.download = decodeURIComponent(filename); //下载后文件名
        document.body.appendChild(downloadElement);
        downloadElement.click(); //点击下载
        document.body.removeChild(downloadElement); //下载完成移除元素
        window.URL.revokeObjectURL(href); //释放掉blob对象
    } else {
        return response.data;
    }
}, function (error) {
    const vm = new Vue();
    const {rtnFlag, rtnMsg} = error.response.data;
    vm.$message.error(rtnMsg || "操作异常,请联系管理员");
    return Promise.reject(error);
});