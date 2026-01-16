<template>
  <div class="center-container" style="margin-top: 15px;width: 65%;margin: 0 auto">
    <!-- 用户信息卡片 -->
    <a-card class="user-profile-card">
      <div class="user-profile-header">
        <a-avatar
          :src="'http://127.0.0.1:9527/imagesWeb/' + userProfile.images"
          :size="120"
          class="user-avatar"
          shape="circle"
        />
        <div class="user-info">
          <h2 class="user-name">{{ userProfile.name }}</h2>
          <p class="user-code">
            <a-icon type="user" style="margin-right: 8px; color: #4CAF50;" />
            用户编号: {{ userProfile.code }}
          </p>
          <p class="user-content">
            <a-icon type="edit" style="margin-right: 8px; color: #81c784;" />
            {{ userProfile.content || '暂无个人简介' }}
          </p>
          <div class="user-stats">
            <span class="stat-item">
              <a-icon type="eye" />
              总浏览: {{ totalViews }}
            </span>
            <span class="stat-item">
              <a-icon type="calendar" />
              加入时间: {{ timeFormat(userProfile.createDate) }}
            </span>
          </div>
        </div>
      </div>
    </a-card>

    <!-- 帖子列表 -->
    <a-row :gutter="8" class="count-info" style="margin-top: 15px">
      <a-card class="head-info-card" style="margin: 0 auto">
        <a-row>
          <a-col :span="24">
            <a-input-search placeholder="搜索贴子" v-show="!postDetailShow" style="width: 200px;margin-top: 10px;float: right" @search="onSearch" />
          </a-col>
        </a-row>
        <a-tabs :activeKey="tabKey" tab-position="top" @change="tabChange" v-show="!postDetailShow">
          <a-tab-pane v-for="item in tagList" :key="item.id" :tab="item.name">
            <a-skeleton active v-if="loading" />
            <div v-if="!loading">
              <a-list item-layout="vertical" size="large" :pagination="pagination" :data-source="postList">
                <a-list-item slot="renderItem" key="item.title" slot-scope="item, index">
                  <template slot="actions">
                    <span key="message">
                      <a-icon type="message" style="margin-right: 8px" />
                      <span v-if="item.collect === 0">{{ item.reply }}</span>
                      <span v-else>{{ item.reply / item.collect }}</span> 回复
                    </span>
                    <span key="star">
                      <a-icon type="star" style="margin-right: 8px" />
                      {{ item.collect }} 收藏
                    </span>
                    <span key="to-top">
                      <a-icon type="to-top" style="margin-right: 8px" />
                      {{ timeFormat(item.createDate) }}
                    </span>
                  </template>
                  <a-list-item-meta :description="item.content.slice(0, 100) + '...'">
                    <a slot="title" @click="postReplyDetail(item)">{{ item.title }}</a>
                    <a-avatar shape="square" slot="avatar" icon="user" :src="'http://127.0.0.1:9527/imagesWeb/' + item.userImages" />
                  </a-list-item-meta>
                </a-list-item>
              </a-list>
            </div>
          </a-tab-pane>
        </a-tabs>
        <div v-if="postDetailShow && postDetail !== null" style="margin: 18px">
          <div style="margin-bottom: 10px">
            <a-breadcrumb>
              <a-breadcrumb-item><a @click="postDetailShow = false">返回</a></a-breadcrumb-item>
              <a-breadcrumb-item>{{ tabName }}</a-breadcrumb-item>
            </a-breadcrumb>
          </div>
          <p style="font-size: 22px;color: black;font-weight: 500;line-height: 150%;margin: 25px 50px;margin-top: 50px">
            {{ postDetail.title }}
            <a-icon type="form" style="cursor: pointer" v-if="currentUser.userId === postDetail.userId" @click="edit(postDetail)"/>
          </p>
          <div style="margin: 25px 50px;font-size: 13px">
            <a-icon v-if="currentUser.userId != postDetail.userId && collectUser === 0" type="heart" style="margin-right: 10px;cursor: pointer" @click="collectUserCheck(0)"/>
            <a-icon v-if="currentUser.userId != postDetail.userId && collectUser > 0" type="heart" style="margin-right: 10px;color: red;cursor: pointer" @click="collectUserCheck(1)"/>
            {{ postDetail.username }} <span v-if="currentUser.userId != postDetail.userId">关注</span>
            <a-divider type="vertical" />
            <a-icon type="eye" style="margin-right: 10px;margin-left: 40px" />
            {{ postDetail.pageviews }} 访问
            <a-divider type="vertical" />
            <a-icon type="message" style="margin-right: 10px" />
            <span v-if="postDetail.collect === 0">{{ postDetail.reply }}</span>
            <span v-else>{{ postDetail.reply / postDetail.collect }}</span> 回复
            <a-divider type="vertical" />
            <a-icon v-if="currentUser.userId != postDetail.userId && collectPost === 0" type="star" style="margin-right: 10px;cursor: pointer" @click="collectPostCheck(0)"/>
            <a-icon v-if="currentUser.userId != postDetail.userId && collectPost > 0" type="star" style="margin-right: 10px;color: red;cursor: pointer" @click="collectPostCheck(1)"/>
            {{ postDetail.collect }} <span v-if="currentUser.userId != postDetail.userId">收藏</span>
            <a-divider type="vertical" />
            {{ timeFormat(postDetail.createDate) }}
          </div>
          <div style="margin: 25px 50px;font-size: 15px;line-height: 1.6;word-break: break-word;letter-spacing: 1px;text-indent: 30px">
            {{ postDetail.content }}
          </div>
          <div style="margin: 25px 50px;height: 100px">
            <a-upload
              name="avatar"
              action="http://127.0.0.1:9527/file/fileUpload/"
              list-type="picture-card"
              :file-list="fileList"
              @preview="handlePreview"
            >
            </a-upload>
            <a-modal :visible="previewVisible" :footer="null" @cancel="handleCancel">
              <img alt="example" style="width: 100%" :src="previewImage" />
            </a-modal>
          </div>
          <div style="margin: 25px 50px;">
            <a-list
              class="comment-list"
              :pagination="pagination"
              :header="`${replyList.length} 回复`"
              item-layout="horizontal"
              :data-source="replyList"
            >
              <a-list-item slot="renderItem" slot-scope="item, index">
                <a-comment :author="item.username" shape="square" :avatar="'http://127.0.0.1:9527/imagesWeb/' + item.images">
                  <template slot="actions">
                    <span @click="replyUserAdd(item)">回复</span>
                  </template>
                  <p slot="content" style="white-space: pre-line;">
                    {{ item.content }}
                  </p>
                  <a-tooltip slot="datetime" :title="item.sendCreate">
                    <span>{{ timeFormat(item.sendCreate) }}</span>
                  </a-tooltip>
                </a-comment>
              </a-list-item>
            </a-list>
<!--            <div style="margin-bottom: 200px;margin-top: 50px">-->
<!--              <a-textarea-->
<!--                v-model="replyContent"-->
<!--                placeholder="Controlled autosize"-->
<!--                :rows="5"-->
<!--              />-->
<!--              <a-button type="primary" style="float: right;margin-top: 15px" @click="commit">-->
<!--                提交-->
<!--              </a-button>-->
<!--            </div>-->
          </div>
        </div>
      </a-card>
    </a-row>
    <post-add
      v-if="postAdd.visiable"
      @close="handlepostAddClose"
      @success="handlepostAddSuccess"
      :postAddVisiable="postAdd.visiable"
      :tagList="tagListData">
    </post-add>
    <post-edit
      ref="postEdit"
      @close="handlepostEditClose"
      @success="handlepostEditSuccess"
      :postEditVisiable="postEdit.visiable"
      :tagList="tagListData">
    </post-edit>
  </div>
</template>
<script>
import {mapState} from 'vuex'
import moment from 'moment'
moment.locale('zh-cn')
function getBase64 (file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => resolve(reader.result)
    reader.onerror = error => reject(error)
  })
}
export default {
  name: 'Center',
  computed: {
    ...mapState({
      currentUser: state => state.account.user
    })
  },
  data () {
    return {
      userProfile: {},
      totalViews: 0,
      pagination: {
        pageSize: 20
      },
      tagList: [],
      tagListData: [],
      postList: [],
      replyList: [],
      postDetail: null,
      tabName: '',
      tabKey: '',
      postDetailShow: false,
      loading: false,
      fileList: [],
      previewVisible: false,
      previewImage: '',
      replyContent: '',
      replyUser: null,
      collectPost: 0,
      collectUser: 0,
      postAdd: {
        visiable: false
      },
      postEdit: {
        visiable: false
      }
    }
  },
  mounted () {
    this.queryContacts()
    this.getTagList()
  },
  methods: {
    queryContacts () {
      this.$get(`/cos/post-info/user/detail`, {
        userId: this.currentUser.userId
      }).then((r) => {
        if (r.data.code === 0) {
          this.userProfile = r.data.user
          this.postList = r.data.post
          // 计算总浏览量
          this.totalViews = this.postList.reduce((sum, post) => sum + post.pageviews, 0)
        } else {
          this.$message.error('获取用户信息失败')
        }
      }).catch(error => {
        console.error('获取用户信息失败:', error)
        this.$message.error('获取用户信息失败')
      })
    },
    timeFormat (time) {
      return moment(time).format('YYYY-MM-DD HH:mm:ss')
    },
    collectUserCheck (deleteFlag) {
      this.$post(`/cos/focus-info`, {userId: this.currentUser.userId, collectUserId: this.postDetail.userId, deleteFlag}).then((r) => {
        this.postReplyDetail(this.postDetail)
        this.$message.success(deleteFlag === 0 ? '关注成功！' : '取消关注成功！')
      })
    },
    collectPostCheck (deleteFlag) {
      this.$post(`/cos/collect-info`, {userId: this.currentUser.userId, postId: this.postDetail.id, deleteFlag}).then((r) => {
        this.postReplyDetail(this.postDetail)
        this.$message.success(deleteFlag === 0 ? '收藏贴子成功！' : '取消收藏成功！')
      })
    },
    collectByUser (postId) {
      this.$get(`/cos/post-info/collcet`, {userId: this.currentUser.userId, postId}).then((r) => {
        this.collectPost = r.data.collect
        this.collectUser = r.data.focus
      })
    },
    commit () {
      if (this.replyContent !== '') {
        let data = {userId: this.currentUser.userId, content: this.replyContent, postId: this.postDetail.id, replyUserId: this.replyUser}
        this.$post(`/cos/reply-info`, data).then((r) => {
          if (r.data.code === 500) {
            this.$message.error(r.data.msg)
          } else {
            this.postReplyDetail(this.postDetail)
            this.replyContent = ''
          }
        })
      } else {
        this.$message.error('请填写评论！')
      }
    },
    replyUserAdd (reply) {
      this.replyUser = reply.userId
      this.replyContent = this.replyContent + '@' + reply.username
    },
    postReplyDetail (post) {
      this.postInfoDetail(post.id)
      this.collectByUser(post.id)
      this.replyUser = []
      this.fileList = []
      this.$get(`/cos/reply-info/list/${post.id}`).then((r) => {
        this.replyList = r.data.data
        this.postDetailShow = true
      })
    },
    postInfoDetail (postId) {
      this.$get(`/cos/post-info/${postId}`).then((r) => {
        this.postDetail = r.data
        this.imagesInit(this.postDetail.images)
      })
    },
    tabChange (key) {
      this.tabName = this.tagList.find(o => o.id === key).name
      this.tabKey = key
      if (key !== 9999 && key !== -1) {
        this.getPostList(key)
        if (this.tagList[this.tagList.length - 1].id === 9999) {
          this.tagList.pop()
        }
      }
    },
    imagesInit (images) {
      if (images !== null && images !== '') {
        let imageList = []
        images.split(',').forEach((image, index) => {
          imageList.push({uid: index, name: image, status: 'done', url: 'http://127.0.0.1:9527/imagesWeb/' + image})
        })
        this.fileList = imageList
      }
    },
    async handlePreview (file) {
      if (!file.url && !file.preview) {
        file.preview = await getBase64(file.originFileObj)
      }
      this.previewImage = file.url || file.preview
      this.previewVisible = true
    },
    handleCancel () {
      this.previewVisible = false
    },
    getPostList (tagId) {
      this.loading = true
      this.$get(`/cos/post-info/tagByUser`, {
        tagId,
        userId: this.currentUser.userId
      }).then((r) => {
        this.postList = r.data.data
        setTimeout(() => {
          this.loading = false
        }, 500)
      })
    },
    getTagList () {
      this.$get('/cos/tag-info/list').then((r) => {
        this.tagList.push.apply(this.tagList, r.data.data)
        if (this.tagList.length !== 0) {
          this.tabChange(this.tagList[0].id)
        }
        let tagListData = []
        r.data.data.forEach(item => {
          tagListData.push({label: item.name, value: item.id})
        })
        this.tagListData = tagListData
      })
    },
    add () {
      this.postAdd.visiable = true
    },
    handlepostAddClose () {
      this.postAdd.visiable = false
    },
    handlepostAddSuccess () {
      this.postAdd.visiable = false
      this.$message.success('新增贴子成功')
      this.getPostList(this.tabKey)
    },
    edit (record) {
      this.$refs.postEdit.setFormValues(record)
      this.postEdit.visiable = true
    },
    handlepostEditClose () {
      this.postEdit.visiable = false
    },
    handlepostEditSuccess () {
      this.postEdit.visiable = false
      this.$message.success('修改贴子成功')
      this.postReplyDetail(this.postDetail)
    },
    onSearch (key) {
      if (key !== '') {
        this.loading = true
        if (this.tagList[this.tagList.length - 1].id !== 9999) {
          this.tagList.push({id: 9999, name: '搜索'})
        }
        this.tabKey = 9999
        this.tabName = '搜索'
        this.$get(`/cos/post-info/listByUser`, {
          key,
          userId: this.currentUser.userId
        }).then((r) => {
          this.postList = r.data.data
          setTimeout(() => {
            this.loading = false
          }, 500)
        })
      }
    }
  }
}
</script>

<style scoped lang="less">
.center-container {
  padding: 20px 0;
}

.user-profile-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8fdf7 100%);
  border-radius: 6px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
  border: none;
  overflow: hidden;
  margin-bottom: 20px;
}

.user-profile-header {
  display: flex;
  align-items: center;
  padding: 30px;
  gap: 30px;
}

.user-avatar {
  border: 4px solid #e8f5e9;
  box-shadow: 0 6px 12px rgba(0,0,0,0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 8px 16px rgba(0,0,0,0.15);
}

.user-info {
  flex: 1;
  text-align: left;
}

.user-name {
  margin: 0 0 10px 0;
  color: #2e7d32;
  font-size: 28px;
  font-weight: 600;
  position: relative;
  display: inline-block;
}

.user-name:after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 60px;
  height: 3px;
  background: #4CAF50;
  border-radius: 2px;
}

.user-code {
  margin: 10px 0;
  color: #66bb6a;
  font-size: 16px;
  font-weight: 500;
}

.user-content {
  margin: 15px 0;
  color: #757575;
  font-size: 16px;
  line-height: 1.6;
  font-style: italic;
  padding: 12px 16px;
  background: #f1f8e9;
  border-radius: 6px;
  border-left: 4px solid #81c784;
}

.user-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-top: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: linear-gradient(90deg, #e8f5e9, #c8e6c9);
  border-radius: 6px;
  color: #2e7d32;
  font-weight: 500;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  transition: transform 0.2s ease;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.count-info {
  .head-info-card {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-radius: 6px;
    padding: 20px;
    box-shadow: 0 8px 24px rgba(0,0,0,0.08);
    border: 1px solid rgba(232, 245, 233, 0.5);
  }

  .ant-input-search {
    .ant-input {
      border-radius: 6px;
      border: 1px solid #c8e6c9;
      padding: 6px 20px;
      &:focus {
        border-color: #4CAF50;
        box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.2);
      }
    }
    .ant-input-search-button {
      border-radius: 6px;
      background: linear-gradient(90deg, #4CAF50, #2E7932);
      border: none;
    }
  }

  .ant-tabs {
    .ant-tabs-tab {
      padding: 12px 20px !important;
      border-radius: 8px;
      margin-right: 8px;
      transition: all 0.3s;
      border: 1px solid transparent;
      &:hover {
        background: #e8f5e9;
      }
    }
    .ant-tabs-tab-active {
      background: #e8f5e9;
      color: #4CAF50;
      font-weight: 600;
      border: 1px solid #c8e6c9;
      border-bottom: 1px solid #fff;
    }
    .ant-tabs-ink-bar {
      background: #4CAF50;
      height: 3px;
    }
  }

  .ant-list-item {
    padding: 20px !important;
    border-radius: 6px;
    margin-bottom: 15px;
    border: 1px solid #f0f0f0;
    transition: all 0.3s cubic-bezier(0.215, 0.61, 0.355, 1);

    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 6px 16px rgba(0,0,0,0.08);
      border-color: #c8e6c9;
    }

    .ant-list-item-meta-title {
      font-size: 18px;
      font-weight: 600;
      color: #2e7d32;

      a {
        color: #2e7d32;
        transition: color 0.3s;

        &:hover {
          color: #4CAF50;
        }
      }
    }

    .ant-list-item-meta-description {
      color: #666;
      line-height: 1.7;
    }

    .ant-list-item-action {
      li {
        span {
          display: flex;
          align-items: center;
          color: #66bb6a;
          font-size: 14px;

          .anticon {
            margin-right: 5px;
            font-size: 16px;
          }
        }
      }
    }
  }
}

.post-detail-section {
  background: white;
  border-radius: 6px;
  padding: 2rem;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);

  .post-title {
    font-size: 1.8rem;
    color: #1b5e20;
    font-weight: 600;
    margin-bottom: 1.5rem;
    line-height: 1.4;
  }

  .post-meta {
    background: #f5fbf5;
    padding: 1rem;
    border-radius: 8px;
    margin-bottom: 1.5rem;
    border-left: 4px solid #4CAF50;

    .meta-item {
      display: inline-flex;
      align-items: center;
      margin-right: 1.5rem;
      color: #66bb6a;

      .anticon {
        margin-right: 8px;
      }
    }
  }

  .post-content {
    font-size: 1.05rem;
    line-height: 1.8;
    color: #424242;
    margin-bottom: 2rem;
    padding: 1.5rem;
    background: #fafafa;
    border-radius: 8px;
    border-left: 4px solid #81c784;
  }
}

.comment-list {
  .ant-list-header {
    background: #e8f5e9;
    border-radius: 8px 8px 0 0;
    font-weight: 500;
    color: #2e7d32;
  }

  .ant-comment {
    padding: 1rem 0;
    border-bottom: 1px solid #eee;

    .ant-comment-content {
      background: #f1f8e9;
      padding: 1rem;
      border-radius: 8px;

      .ant-comment-author-time {
        color: #9e9e9e;
        font-size: 0.85rem;
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-profile-header {
    flex-direction: column;
    text-align: center;
    gap: 15px;
  }

  .user-info {
    text-align: center;
  }

  .user-stats {
    justify-content: center;
  }

  .center-container {
    width: 95% !important;
  }
}
</style>
