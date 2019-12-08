<template>
  <div>
    <v-toolbar flat color="white">
      <!--<v-toolbar-title>My CRUD 11</v-toolbar-title>-->
      <!--<v-divider-->
              <!--class="mx-2"-->
              <!--inset-->
              <!--vertical-->
      <!--&gt;</v-divider>-->


      <v-dialog v-model="dialog" max-width="500px">
        <template v-slot:activator="{ on }">
          <!--<v-card-title>-->
          <v-btn color="primary" dark class="mb-2" v-on="on">新增</v-btn>
          <!--相当于占位符，空格-->
          <v-spacer/>
          <v-spacer/>
          <!--搜索框，与search属性关联-->
          <v-text-field label="输入关键字搜索" hide-details v-model="search" append-icon="search"/>
          <!--</v-card-title>-->
        </template>
        <!--<v-text-field label="输入关键字搜索" hide-details v-model="search" append-icon="search"/>-->
        <v-card>
          <v-card-title>
            <span class="headline">{{ formTitle }}</span>
          </v-card-title>


          <!--新增页面的弹框-->
          <v-card-text>
            <v-container grid-list-md>
              <v-layout wrap>
                <v-flex xs12 sm6 md4 >
                  <v-text-field v-model="editedItem.id" label="id" ></v-text-field>
                </v-flex>
                <v-flex xs12 sm6 md4>
                  <v-text-field v-model="editedItem.name" label="name"></v-text-field>
                </v-flex>
                <v-flex xs12 sm6 md4>
                  <v-text-field v-model="editedItem.image" label="image"></v-text-field>
                </v-flex>
                <v-flex xs12 sm6 md4>
                  <v-text-field v-model="editedItem.letter" label="letter"></v-text-field>
                </v-flex>
              </v-layout>
            </v-container>
          </v-card-text>
          <!--保存-->
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="blue darken-1" flat @click="close">Cancel</v-btn>
            <v-btn color="blue darken-1" flat @click="save">Save</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </v-toolbar>
    <!--数据的列和表头的列-->
    <v-data-table
            :headers="headers"
            :items="brands"
            class="elevation-1"
    >
      <!--数据的列-->
      <template v-slot:items="props">
        <td class="text-xs-center">{{ props.item.id }}</td>
        <td class="text-xs-center">{{ props.item.name }}</td>
        <td class="text-xs-center">{{ props.item.image }}</td>
        <td class="text-xs-center">{{ props.item.letter }}</td>
        <td class="justify-center layout px-0">
          <v-icon
                  small
                  class="mr-2"
                  @click="editItem(props.item)"
          >
            edit
          </v-icon>
          <v-icon
                  small
                  @click="deleteItem(props.item)"
          >
            delete
          </v-icon>
        </td>
      <!--</template>
      <template v-slot:no-data>
        <v-btn color="primary" @click="initialize">Reset</v-btn>
      </template>-->
    </v-data-table>
  </div>
</template>

<script>
    export default {
        data() {
            return {
                dialog: false,     //设置会话框的打开   false不打开，true打开
                headers: [
                    {
                        text: 'id',
                        align: 'center',
                        sortable: false,
                        value: 'id'
                    },
                    {text: '名称', align: 'center', value: 'name'},
                    {text: 'LOGO', align: 'center', value: 'image'},
                    {text: '首字母', align: 'center', value: 'letter'},
                    {text: 'Actions', align: 'center', value: 'id', sortable: false}
                ],
                search: '',  //搜索过滤字段
                totalBrands: 0,  //总条数
                brands: [],
                pagination: {},     //分页需要定义该对象
                editedIndex: -1,
                editedItem: {     //新增时的默认数据
                    id: '',
                    name: 0,
                    image: 0,
                    letter: 0
                },
                defaultItem: {     //新增时close的默认数据
                    id: '',
                    name: 0,
                    image: 0,
                    letter: 0
                }
            }
        },

        computed: {
            formTitle () {
                return this.editedIndex === -1 ? 'New Item' : 'Edit Item'
            }
        },
        mounted(){
            this.getDataFromServer();
        },
        watch: {
            dialog (val) {
                val || this.close()
            },
            pagination: {   //监视pagination属性的变化
                deep: true,   //deep为true，会监视pagination的属性及属性中的对象属性变化
                handler(){
                    //变化后的回调函数
                    this.getDataFromServer();
                }
            },
            serch: {  //监视搜索字段
                handler(){
                    this.getDataFromServer();
                }
            }
        },

        created () {
            this.getDataFromServer()
        },

        methods: {
            initialize () {
                this.brands = [
                    {
                        id: 'Frozen Yogurt',
                        name: 159,
                        image: 6.0,
                        letter: 24
                    },
                    {
                        id: 'Ice cream sandwich',
                        name: 237,
                        image: 9.0,
                        letter: 37
                    },
                    {
                        id: 'Eclair1',
                        name: 262,
                        image: 16.0,
                        letter: 23
                    },
                    {
                        id: 'Eclair2',
                        name: 262,
                        image: 16.0,
                        letter: 23
                    },
                    {
                        id: 'Eclair3',
                        name: 262,
                        image: 16.0,
                        letter: 23
                    },
                    {
                        id: 'Eclair4',
                        name: 262,
                        image: 16.0,
                        letter: 23
                    },
                    {
                        id: 'Eclai5r',
                        name: 262,
                        image: 16.0,
                        letter: 23
                    },
                    {
                        id: 'Eclai6r',
                        name: 262,
                        image: 16.0,
                        letter: 23
                    },
                    {
                        id: 'Eclai3r',
                        name: 262,
                        image: 16.0,
                        letter: 23
                    },
                    {
                        id: 'Ecla5ir',
                        name: 262,
                        image: 16.0,
                        letter: 23
                    }
                ]
            },
            getDataFromServer(){
                this.$http.get("/item/brand/page",{
                    params:{
                        key: this.search,
                        page: this.pagination.page,  //当前页
                        rows: this.pagination.rowsPerPage, //每页大小
                        sorBy: this.pagination.sorBy,  //排序字段
                        desc: this.pagination.descending   //是否降序
                    }
                }).then(resp => {
                    console.log(resp);
                    this.brands = resp.data.items;
                    this.totalBrands = resp.data.total;
                })
            },

            editItem (item) {
                this.editedIndex = this.brands.indexOf(item)
                this.editedItem = Object.assign({}, item)
                this.dialog = true
            },

            deleteItem (item) {
                const index = this.brands.indexOf(item)
                confirm('Are you sure you want to delete this item?') && this.brands.splice(index, 1)
            },

            close () {
                this.dialog = false
                setTimeout(() => {
                    //Object.assign方法用来将源对象（source）的所有可枚举属性，复制到目标对象（target）。它至少需要两个对象作为参数，第一个参数
                    // 是目标对象，后面的参数都是源对象。
                    this.editedItem = Object.assign({}, this.defaultItem)
                    this.editedIndex = -1
                }, 300)
            },

            save () {
                if (this.editedIndex > -1) {
                    Object.assign(this.brands[this.editedIndex], this.editedItem)
                } else {
                    this.brands.push(this.editedItem)
                }
                this.close()
            }
        }
    }
</script>

<style scoped>

</style>
