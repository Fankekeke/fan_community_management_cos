<template>
  <a-card :bordered="false" class="card-area">
    <div :class="advanced ? 'search' : null">
      <!-- 搜索区域 -->
      <a-form layout="horizontal">
        <a-row :gutter="15">
          <div :class="advanced ? null: 'fold'">
            <a-col :md="6" :sm="24">
              <a-form-item
                label="直播标题"
                :labelCol="{span: 8}"
                :wrapperCol="{span: 15, offset: 1}">
                <a-input v-model="queryParams.title"/>
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
        <a-button type="primary" ghost @click="add">新增</a-button>
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
          <a-divider type="vertical" />
          <a-icon type="setting" theme="twoTone" twoToneColor="#4a9ff5" @click="edit(record)" title="修 改"></a-icon>
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
    <!-- 直播详情弹窗 -->
    <!-- 直播详情弹窗 -->
    <a-modal v-model="onlineDetail.visiable" title="直播详情" :width="800" @cancel="handleDetailClose">
      <template slot="footer">
        <a-button key="close" @click="handleDetailClose">关闭</a-button>
        <a-button key="copy" @click="copyLiveUrl(selectedRecord.liveUrl)">复制链接</a-button>
        <a-button key="play" type="primary" @click="playLiveStream(selectedRecord.liveUrl)">播放直播</a-button>
      </template>
      <div class="detail-content">
        <div class="detail-item">
          <span class="detail-label">直播标题：</span>
          <span class="detail-value">{{ selectedRecord.title }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">直播链接：</span>
          <a-tooltip :title="selectedRecord.liveUrl">
        <span class="detail-value detail-link" @click="copyLiveUrl(selectedRecord.liveUrl)">
          {{ selectedRecord.liveUrl && selectedRecord.liveUrl.length > 50 ? selectedRecord.liveUrl.substring(0, 50) + '...' : selectedRecord.liveUrl }}
        </span>
          </a-tooltip>
        </div>
        <div class="detail-item">
          <span class="detail-label">是否仅限会员：</span>
          <span class="detail-value" :class="{'vip-status': selectedRecord.isVipOnly === '1', 'normal-status': selectedRecord.isVipOnly === '0'}">
        {{ selectedRecord.isVipOnly === '1' ? '是' : '否' }}
      </span>
        </div>
        <div class="detail-item">
          <span class="detail-label">状态：</span>
          <span class="detail-value" :class="{'status-active': selectedRecord.status === '1', 'status-inactive': selectedRecord.status === '0'}">
        {{ selectedRecord.status === '1' ? '开启' : '禁用' }}
      </span>
        </div>
        <div class="detail-item">
          <span class="detail-label">开始时间：</span>
          <span class="detail-value">{{ selectedRecord.startTime }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">结束时间：</span>
          <span class="detail-value">{{ selectedRecord.endTime }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">创建时间：</span>
          <span class="detail-value">{{ selectedRecord.createdAt }}</span>
        </div>
        <div class="detail-item" v-if="selectedRecord.coverImage">
          <span class="detail-label">封面图：</span>
          <div class="cover-image-container">
            <img :src="'http://127.0.0.1:9527/imagesWeb/' + selectedRecord.coverImage" alt="封面图" class="cover-image" />
          </div>
        </div>

        <!-- 视频播放器区域 -->
        <div class="video-player-container" v-if="currentLiveUrl">
          <h4>直播预览</h4>
          <video
            id="live-player"
            class="video-js vjs-default-skin"
            controls
            preload="auto"
            width="100%"
            height="400"
            :key="playerKey"
          >
            <source :src="currentLiveUrl" type="application/x-mpegURL" />
          </video>
        </div>
      </div>
    </a-modal>
  </a-card>
</template>

<script>
import RangeDate from '@/components/datetime/RangeDate'
import BulletinAdd from './OnlineAdd.vue'
import BulletinEdit from './OnlineEdit.vue'
import {mapState} from 'vuex'
import videojs from 'video.js';
import 'video.js/dist/video-js.css';
import moment from 'moment'
moment.locale('zh-cn')

export default {
  name: 'Bulletin',
  components: {BulletinAdd, BulletinEdit, RangeDate},
  data () {
    return {
      // ...其他数据
      onlineDetail: {
        visiable: false
      },
      currentLiveUrl: null, // 当前播放的直播链接
      playerKey: 0, // 用于强制刷新播放器
      player: null, // 播放器实例
      selectedRecord: {},
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
        title: '直播标题',
        dataIndex: 'title',
        scopedSlots: { customRender: 'titleShow' },
        width: 200
      }, {
        title: '封面图',
        dataIndex: 'coverImage',
        width: 120,
        customRender: (text, record) => {
          if (text) {
            return <a-popover>
              <template slot="content">
                <img src={`http://127.0.0.1:9527/imagesWeb/${text}`} style="width: 200px; height: auto;" alt="封面图" />
              </template>
              <a-avatar shape="square" size="large" src={`http://127.0.0.1:9527/imagesWeb/${text}`} />
            </a-popover>
          } else {
            return <a-avatar shape="square" size="large" icon="picture" />
          }
        }
      }, {
        title: '直播链接',
        dataIndex: 'liveUrl',
        width: 200,
        customRender: (text) => {
          if (text) {
            return text.length > 20 ? `${text.substring(0, 20)}...` : text
          } else {
            return '- -'
          }
        }
      }, {
        title: '是否仅限会员',
        dataIndex: 'isVipOnly',
        width: 100,
        customRender: (text) => text === '1' ? '是' : '否'
      }, {
        title: '状态',
        dataIndex: 'status',
        width: 80,
        customRender: (text) => text === '1' ? '开启' : '禁用'
      }, {
        title: '开始时间',
        dataIndex: 'startTime',
        width: 150,
        customRender: (text) => {
          if (text) {
            return text
          } else {
            return '- -'
          }
        }
      }, {
        title: '结束时间',
        dataIndex: 'endTime',
        width: 150,
        customRender: (text) => {
          if (text) {
            return text
          } else {
            return '- -'
          }
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
    initPlayer () {
      // 销毁之前的播放器实例
      if (this.player) {
        this.player.dispose()
      }

      // 使用异步延迟确保DOM已渲染
      this.$nextTick(() => {
        setTimeout(() => {
          const videoElement = document.getElementById('live-player')
          if (videoElement) {
            // 如果使用 video.js
            this.player = this.$video(videoElement, {
              language: 'zh-CN',
              playbackRates: [0.5, 1, 1.5, 2],
              fluid: true,
              responsive: true
            })
          }
        }, 100)
      })
    },

    // 播放直播流
    playLiveStream (liveUrl) {
      if (!liveUrl) {
        this.$message.warning('直播链接不存在')
        return
      }

      // 设置当前直播链接，触发视频播放器更新
      this.currentLiveUrl = liveUrl

      // 强制刷新播放器
      this.playerKey += 1

      // 初始化播放器
      this.$nextTick(() => {
        this.initPlayer()
      })
    },

    // 查看详情
    viewDetail (record) {
      this.selectedRecord = record
      this.currentLiveUrl = record.liveUrl
      this.onlineDetail.visiable = true

      // 初始化播放器
      this.$nextTick(() => {
        this.playerKey += 1
        setTimeout(() => {
          this.initPlayer()
        }, 100)
      })
    },

    // 关闭详情弹窗
    handleDetailClose () {
      // 销毁播放器实例
      if (this.player) {
        this.player.dispose()
        this.player = null
      }
      this.currentLiveUrl = null
      this.onlineDetail.visiable = false
    },
    async copyLiveUrl (url) {
      if (!url) {
        this.$message.warning('直播链接不存在')
        return
      }

      try {
        await navigator.clipboard.writeText(url)
        this.$message.success('直播链接已复制到剪贴板')
      } catch (err) {
        // 降级方案
        const textArea = document.createElement('textarea')
        textArea.value = url
        document.body.appendChild(textArea)
        textArea.select()
        document.execCommand('copy')
        document.body.removeChild(textArea)
        this.$message.success('直播链接已复制到剪贴板')
      }
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
      this.$message.success('新增线上直播成功')
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
      this.$message.success('修改线上直播成功')
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
          that.$delete('/cos/online-events/' + ids).then(() => {
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
      this.$get('/cos/online-events/page', {
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
  .detail-item {
    display: flex;
    margin-bottom: 15px;
    align-items: flex-start;
    padding: 8px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .detail-label {
      font-weight: bold;
      min-width: 100px;
      color: #666;
      flex-shrink: 0;
    }

    .detail-value {
      flex: 1;
      word-break: break-all;
      color: #333;

      &.detail-link {
        color: #1890ff;
        cursor: pointer;
        transition: color 0.3s;

        &:hover {
          color: #40a9ff;
        }
      }

      &.vip-status {
        color: #ff7875;
      }

      &.normal-status {
        color: #52c41a;
      }

      &.status-active {
        color: #52c41a;
      }

      &.status-inactive {
        color: #bfbfbf;
      }
    }
  }

  .cover-image-container {
    flex: 1;
    margin-top: 5px;

    .cover-image {
      max-width: 200px;
      max-height: 150px;
      border-radius: 4px;
      border: 1px solid #e8e8e8;
      object-fit: cover;
    }
  }
}

// 弹窗整体样式优化
/deep/ .ant-modal-body {
  padding: 24px;
}

.detail-content {
  .detail-item {
    display: flex;
    margin-bottom: 15px;
    align-items: flex-start;
    padding: 8px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .detail-label {
      font-weight: bold;
      min-width: 100px;
      color: #666;
      flex-shrink: 0;
    }

    .detail-value {
      flex: 1;
      word-break: break-all;
      color: #333;

      &.detail-link {
        color: #1890ff;
        cursor: pointer;
        transition: color 0.3s;

        &:hover {
          color: #40a9ff;
        }
      }

      &.vip-status {
        color: #ff7875;
      }

      &.normal-status {
        color: #52c41a;
      }

      &.status-active {
        color: #52c41a;
      }

      &.status-inactive {
        color: #bfbfbf;
      }
    }
  }

  .cover-image-container {
    flex: 1;
    margin-top: 5px;

    .cover-image {
      max-width: 200px;
      max-height: 150px;
      border-radius: 4px;
      border: 1px solid #e8e8e8;
      object-fit: cover;
    }
  }

  .video-player-container {
    margin-top: 20px;
    width: 100%;

    h4 {
      margin-bottom: 10px;
      color: #333;
    }
  }
}

// 弹窗整体样式优化
/deep/ .ant-modal-body {
  padding: 24px;
}

// 视频播放器样式
.video-js {
  width: 100% !important;
  height: 400px !important;
  background-color: #000;
}
</style>
