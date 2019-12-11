Vue.component('datagrid', {
    template: `<div>
           <el-table ref="datagrid" v-loading="loading" :data="tableData" size="mini"
                     border :height="height" stripe fit @select-all="handleSelectAll" @select="handleSelect">
             <el-table-column v-for="column in columns" :type="column.type" :prop="column.prop" :label="column.label"
                              :key="column.prop" :width="column.width" :formatter="column.formatter" show-overflow-tooltip>
             </el-table-column>
             <el-table-column fixed="right" label="操作" :width="actionWidth"
                              v-if="actions !== undefined && actions != null && actions.length > 0">
               <template slot-scope="scope">
                 <el-button type="text" v-for="action in actions" :key="action.text" size="mini" @click="action.callback(scope.row)"
                            v-if="action.checkShow(scope.row)">
                   {{action.label}}
                 </el-button>
               </template>
             </el-table-column>
           </el-table>
           <div align="right" id="pagination" v-if="showPage">
             <el-pagination
               @size-change="handleSizeChange"
               @current-change="handleCurrentChange"
               :current-page="currentPage"
               :page-sizes="[10, 15, 20, 40]"
               :page-rows="pageSize"
               layout="total, sizes, prev, pager, next, jumper"
               :total="total">
             </el-pagination>
           </div>
         </div>`,
    props: {
        // 表格列
        columns: {
            type: [Array, Object],
            default: () => {
            }
        },
        // 表格操作列
        actions: {
            type: [Array, Object],
            default: () => {
            },
            required: false
        },
        // 是否展示分页
        showPage: {
            type: Boolean,
            default: true
        },
        uri: {
            type: String,
            required: true
        },
        height: {
            type: Number,
            required: false,
            default: 466
        },
        actionWidth: {
            type: Number,
            required: false,
            default: 150
        }
    },
    data() {
        return {
            currentPage: 1,
            pageSize: 10,
            total: 0,
            tableData: [],
            inParam: {},
            loading: false,
            hasCheckedData: []
        }
    },
    methods: {
        handleSelectAll(val) {
            let v = this;
            //remove
            if (val.length === 0) {
                for (let i = 0; i < v.tableData.length; i++) {
                    for (let j = 0; j < v.hasCheckedData.length; j++) {
                        if (v.hasCheckedData[j].id === v.tableData[i].id) {
                            v.hasCheckedData.splice(j, 1);
                            break;
                        }
                    }
                }
            }
            if (v.hasCheckedData.length === 0) {
                for (let i = 0; i < val.length; i++) {
                    v.hasCheckedData.push(val[i]);
                }
            } else {
                for (let i = 0; i < val.length; i++) {
                    let flag = false;
                    for (let j = 0; j < v.hasCheckedData.length; j++) {
                        if (v.hasCheckedData[j].id === val[i].id) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag)
                        v.hasCheckedData.push(val[i]);
                }
            }
        },
        handleSelect(val, row) {
            /* 1 => add ; 0 => remove*/
            let flag = 0;
            for (let i = 0; i < val.length; i++) {
                if (row.id === val[i].id) {
                    flag = 1;
                    break;
                }
            }
            if (flag === 1) {
                this.hasCheckedData.push(row);
            } else {
                for (let i = 0; i < this.hasCheckedData.length; i++) {
                    if (this.hasCheckedData[i].id === row.id)
                        this.hasCheckedData.splice(i, 1);
                }
            }
        },
        async query(params) {
            let own = this;
            own.tableData = [];
            own.inParam = params;
            own.loading = true;
            let newObj = own.inParam;
            if (this.showPage) {
                newObj = Object.assign({currentPage: this.currentPage, pageSize: this.pageSize}, this.inParam);
            }
            await http.post(own.uri, newObj).then((response) => {
                const {rows, total, rtnData} = response;
                if (rtnData) {
                    own.tableData = rtnData;
                } else {
                    own.tableData = rows;
                    own.total = total;
                }
                own.loading = false;
                own.hasCheckedData = [];
            }).catch(function (error) {
                console.log(error)
            })
        },
        handleSizeChange(val) { // 每页条数
            this.pageSize = val;
            this.query(this.inParam);
        },
        handleCurrentChange(val) { // 当前页
            this.currentPage = val;
            this.query(this.inParam);
        }
    }
});