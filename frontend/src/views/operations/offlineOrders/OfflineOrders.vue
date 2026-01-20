<template>
  <a-card :bordered="false" class="card-area">
    <div :class="advanced ? 'search' : null">
      <!-- 搜索区域 -->
      <a-form layout="horizontal">
        <a-row :gutter="15">
          <div :class="advanced ? null: 'fold'">
            <a-col :md="6" :sm="24">
              <a-form-item
                label="活动名称"
                :labelCol="{span: 8}"
                :wrapperCol="{span: 15, offset: 1}">
                <a-input v-model="queryParams.title"/>
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item
                label="所属用户"
                :labelCol="{span: 8}"
                :wrapperCol="{span: 15, offset: 1}">
                <a-input v-model="queryParams.username"/>
              </a-form-item>
            </a-col>
          </div>
          <span style="float: right; margin-top: 3px;">
            <a-button type="primary" @click="search">查询</a-button>
            <a-button style="margin-left: 8px" @click="reset">重置</a-button>
          </span>
        </a-row>
      </a-form>
    </div>
    <div>
      <div class="operator">
<!--        <a-button type="primary" ghost @click="add">新增</a-button>-->
        <a-button @click="batchDelete">删除</a-button>
      </div>
      <!-- 表格区域 -->
      <a-table ref="TableInfo"
               :columns="columns"
               :rowKey="record => record.id"
               :dataSource="dataSource"
               :pagination="pagination"
               :loading="loading"
               :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
               :scroll="{ x: 900 }"
               @change="handleTableChange">
        <template slot="titleShow" slot-scope="text, record">
          <template>
            <a-tooltip>
              <template slot="title">
                {{ record.title }}
              </template>
              {{ record.title.slice(0, 8) }} ...
            </a-tooltip>
          </template>
        </template>
        <template slot="contentShow" slot-scope="text, record">
          <template>
            <a-tooltip>
              <template slot="title">
                {{ record.content }}
              </template>
              {{ record.content.slice(0, 30) }} ...
            </a-tooltip>
          </template>
        </template>
        <template slot="operation" slot-scope="text, record">
          <a-icon type="eye" theme="twoTone" twoToneColor="#1890ff" @click="viewDetail(record)" title="查 看"></a-icon>
        </template>
      </a-table>
    </div>
    <bulletin-add
      v-if="bulletinAdd.visiable"
      @close="handleBulletinAddClose"
      @success="handleBulletinAddSuccess"
      :bulletinAddVisiable="bulletinAdd.visiable">
    </bulletin-add>
    <bulletin-edit
      ref="bulletinEdit"
      @close="handleBulletinEditClose"
      @success="handleBulletinEditSuccess"
      :bulletinEditVisiable="bulletinEdit.visiable">
    </bulletin-edit>
    <!-- 订单详情弹窗 -->
    <a-modal v-model="orderDetail.visiable" title="订单详情" :width="600" @cancel="handleDetailClose">
      <template slot="footer">
        <a-button key="close" @click="handleDetailClose">关闭</a-button>
      </template>
      <div class="detail-content">
        <div class="detail-row">
          <span class="detail-label">订单编号：</span>
          <span class="detail-value">{{ selectedOrder.orderSn }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">用户姓名：</span>
          <span class="detail-value">{{ selectedOrder.username }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">活动名称：</span>
          <span class="detail-value">{{ selectedOrder.title }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">活动地点：</span>
          <span class="detail-value">{{ selectedOrder.location }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">活动日期：</span>
          <span class="detail-value">{{ selectedOrder.eventDate }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">门票价格：</span>
          <span class="detail-value">¥{{ selectedOrder.ticketPrice }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">核销码：</span>
          <div class="qr-code-container">
            <div class="qr-code-image" v-if="selectedOrder.checkCode">
              <img :src="'http://127.0.0.1:9527/imagesWeb/' + selectedOrder.checkCode" alt="核销码" />
            </div>
            <div v-else class="no-image">暂无核销码</div>
          </div>
        </div>
        <div class="detail-row">
          <span class="detail-label">是否已核销：</span>
          <span class="detail-value" :class="{'status-success': selectedOrder.isChecked == 1, 'status-error': selectedOrder.isChecked == 0}">
        {{ selectedOrder.isChecked == 1 ? '是' : '否' }}
      </span>
        </div>
        <div class="detail-row">
          <span class="detail-label">支付状态：</span>
          <span class="detail-value" :class="{'status-success': selectedOrder.paymentStatus === 1, 'status-warning': selectedOrder.paymentStatus === 0, 'status-error': selectedOrder.paymentStatus === 2}">
        {{ selectedOrder.paymentStatus === 0 ? '待支付' : selectedOrder.paymentStatus === 1 ? '已支付' : '已退款' }}
      </span>
        </div>
        <div class="detail-row">
          <span class="detail-label">下单时间：</span>
          <span class="detail-value">{{ selectedOrder.createdAt }}</span>
        </div>
      </div>
    </a-modal>
  </a-card>
</template>

<script>
import RangeDate from '@/components/datetime/RangeDate'
import BulletinAdd from './OfflineOrdersAdd.vue'
import BulletinEdit from './OfflineOrdersEdit.vue'
import {mapState} from 'vuex'
import moment from 'moment'
moment.locale('zh-cn')

export default {
  name: 'Bulletin',
  components: {BulletinAdd, BulletinEdit, RangeDate},
  data () {
    return {
      selectedOrder: {},
      orderDetail: {
        visiable: false
      },
      advanced: false,
      bulletinAdd: {
        visiable: false
      },
      bulletinEdit: {
        visiable: false
      },
      queryParams: {},
      filteredInfo: null,
      sortedInfo: null,
      paginationInfo: null,
      dataSource: [],
      selectedRowKeys: [],
      loading: false,
      pagination: {
        pageSizeOptions: ['10', '20', '30', '40', '100'],
        defaultCurrent: 1,
        defaultPageSize: 10,
        showQuickJumper: true,
        showSizeChanger: true,
        showTotal: (total, range) => `显示 ${range[0]} ~ ${range[1]} 条记录，共 ${total} 条记录`
      },
      userList: []
    }
  },
  computed: {
    ...mapState({
      currentUser: state => state.account.user
    }),
    columns () {
      return [{
        title: '活动名称',
        dataIndex: 'title',
        width: 200
      }, {
        title: '商品图片',
        dataIndex: 'userImages',
        customRender: (text, record, index) => {
          if (!record.userImages) return <a-avatar shape="square" icon="user" />
          return <a-popover>
            <template slot="content">
              <a-avatar shape="square" size={132} icon="user" src={ 'http://127.0.0.1:9527/imagesWeb/' + record.userImages.split(',')[0] } />
            </template>
            <a-avatar shape="square" icon="user" src={ 'http://127.0.0.1:9527/imagesWeb/' + record.userImages.split(',')[0] } />
          </a-popover>
        }
      }, {
        title: '用户名',
        dataIndex: 'username',
        width: 120,
        customRender: (text) => {
          if (text) {
            return text
          } else {
            return '- -'
          }
        }
      }, {
        title: '活动地点',
        dataIndex: 'location',
        width: 200,
        customRender: (text) => {
          if (text) {
            return text.length > 15 ? `${text.substring(0, 15)}...` : text
          } else {
            return '- -'
          }
        }
      }, {
        title: '门票价格',
        dataIndex: 'ticketPrice',
        width: 100,
        customRender: (text) => `¥${text}`
      }, {
        title: '核销码',
        dataIndex: 'checkCode',
        width: 150,
        customRender: (text, record, index) => {
          if (!record.checkCode) return <a-avatar shape="square" icon="user" />
          return <a-popover>
            <template slot="content">
              <a-avatar shape="square" size={132} icon="user" src={ 'http://127.0.0.1:9527/imagesWeb/' + record.checkCode.split(',')[0] } />
            </template>
            <a-avatar shape="square" icon="user" src={ 'http://127.0.0.1:9527/imagesWeb/' + record.checkCode.split(',')[0] } />
          </a-popover>
        }
      }, {
        title: '是否已核销',
        dataIndex: 'isChecked',
        width: 100,
        customRender: (text) => text == 1 ? '是' : '否'
      }, {
        title: '支付状态',
        dataIndex: 'paymentStatus',
        width: 100,
        customRender: (text) => {
          const statusMap = { 0: '待支付', 1: '已支付', 2: '已退款' }
          return statusMap[text] || '未知状态'
        }
      }, {
        title: '创建时间',
        dataIndex: 'createdAt',
        width: 150,
        customRender: (text) => {
          if (text) {
            return text
          } else {
            return '- -'
          }
        }
      }, {
        title: '操作',
        dataIndex: 'operation',
        width: 100,
        scopedSlots: {customRender: 'operation'}
      }]
    }
  },
  mounted () {
    this.fetch()
  },
  methods: {
    handleDetailClose () {
      this.orderDetail.visiable = false
    },
    viewDetail (record) {
      // 设置选中的订单数据
      this.selectedOrder = record
      // 显示详情弹窗
      this.orderDetail.visiable = true
    },
    onSelectChange (selectedRowKeys) {
      this.selectedRowKeys = selectedRowKeys
    },
    toggleAdvanced () {
      this.advanced = !this.advanced
    },
    add () {
      this.bulletinAdd.visiable = true
    },
    handleBulletinAddClose () {
      this.bulletinAdd.visiable = false
    },
    handleBulletinAddSuccess () {
      this.bulletinAdd.visiable = false
      this.$message.success('新增活动预约成功')
      this.search()
    },
    edit (record) {
      this.$refs.bulletinEdit.setFormValues(record)
      this.bulletinEdit.visiable = true
    },
    handleBulletinEditClose () {
      this.bulletinEdit.visiable = false
    },
    handleBulletinEditSuccess () {
      this.bulletinEdit.visiable = false
      this.$message.success('修改活动预约成功')
      this.search()
    },
    handleDeptChange (value) {
      this.queryParams.deptId = value || ''
    },
    batchDelete () {
      if (!this.selectedRowKeys.length) {
        this.$message.warning('请选择需要删除的记录')
        return
      }
      let that = this
      this.$confirm({
        title: '确定删除所选中的记录?',
        content: '当您点击确定按钮后，这些记录将会被彻底删除',
        centered: true,
        onOk () {
          let ids = that.selectedRowKeys.join(',')
          that.$delete('/cos/offline-event-orders/' + ids).then(() => {
            that.$message.success('删除成功')
            that.selectedRowKeys = []
            that.search()
          })
        },
        onCancel () {
          that.selectedRowKeys = []
        }
      })
    },
    search () {
      let {sortedInfo, filteredInfo} = this
      let sortField, sortOrder
      // 获取当前列的排序和列的过滤规则
      if (sortedInfo) {
        sortField = sortedInfo.field
        sortOrder = sortedInfo.order
      }
      this.fetch({
        sortField: sortField,
        sortOrder: sortOrder,
        ...this.queryParams,
        ...filteredInfo
      })
    },
    reset () {
      // 取消选中
      this.selectedRowKeys = []
      // 重置分页
      this.$refs.TableInfo.pagination.current = this.pagination.defaultCurrent
      if (this.paginationInfo) {
        this.paginationInfo.current = this.pagination.defaultCurrent
        this.paginationInfo.pageSize = this.pagination.defaultPageSize
      }
      // 重置列过滤器规则
      this.filteredInfo = null
      // 重置列排序规则
      this.sortedInfo = null
      // 重置查询参数
      this.queryParams = {}
      this.fetch()
    },
    handleTableChange (pagination, filters, sorter) {
      // 将这三个参数赋值给Vue data，用于后续使用
      this.paginationInfo = pagination
      this.filteredInfo = filters
      this.sortedInfo = sorter

      this.fetch({
        sortField: sorter.field,
        sortOrder: sorter.order,
        ...this.queryParams,
        ...filters
      })
    },
    fetch (params = {}) {
      // 显示loading
      this.loading = true
      if (this.paginationInfo) {
        // 如果分页信息不为空，则设置表格当前第几页，每页条数，并设置查询分页参数
        this.$refs.TableInfo.pagination.current = this.paginationInfo.current
        this.$refs.TableInfo.pagination.pageSize = this.paginationInfo.pageSize
        params.size = this.paginationInfo.pageSize
        params.current = this.paginationInfo.current
      } else {
        // 如果分页信息为空，则设置为默认值
        params.size = this.pagination.defaultPageSize
        params.current = this.pagination.defaultCurrent
      }
      this.$get('/cos/offline-event-orders/page', {
        ...params
      }).then((r) => {
        let data = r.data.data
        const pagination = {...this.pagination}
        pagination.total = data.total
        this.dataSource = data.records
        this.pagination = pagination
        // 数据加载完毕，关闭loading
        this.loading = false
      })
    }
  },
  watch: {}
}
</script>
<style lang="less" scoped>
@import "../../../../static/less/Common";

.detail-content {
  .detail-row {
    display: flex;
    margin-bottom: 15px;
    align-items: flex-start;

    .detail-label {
      font-weight: bold;
      min-width: 100px;
      color: #666;
    }

    .detail-value {
      flex: 1;
      word-break: break-all;

      &.status-success {
        color: #52c41a;
      }

      &.status-error {
        color: #f5222d;
      }

      &.status-warning {
        color: #faad14;
      }
    }

    .qr-code-container {
      flex: 1;

      .qr-code-image {
        display: inline-block;

        img {
          width: 150px;
          height: 150px;
          border: 1px solid #ddd;
          padding: 5px;
          border-radius: 4px;
        }
      }

      .no-image {
        color: #ccc;
        display: inline-block;
        line-height: 150px;
      }
    }
  }
}
</style>
