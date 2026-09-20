<?php
/*
Sort:1
Hidden:true
Name:编辑应用
Url:app_edit
Version:1.0
*/
if(!isset($islogin))header("Location: /");//非法访问
$id = isset($_GET['id']) ? intval($_GET['id']) : 0;
$res = Db::table('app')->where(['id'=>$id])->find();
?>						
	<div class="row">
		<div class="col-12">
			<div class="page-title-box">
				<div class="page-title-right">
					<ol class="breadcrumb m-0">
						<li class="breadcrumb-item"><a href="javascript: void(0);">首页</a></li>
						<li class="breadcrumb-item"><a href="./?app_adm">应用管理</a></li>
						<li class="breadcrumb-item active">编辑应用</li>
					</ol>
				</div>
				<h4 class="page-title"><?php echo $title; ?></h4>
			</div> <!-- end page-title-box -->
		</div> <!-- end col-->
	</div>
	<!-- end page title -->
	
	<div class="row">
		<div class="col-lg-12">
			<div class="card">
				<div class="card-body">
					<nav style="overflow:auto;" class="table-responsive">
						<ul class="nav nav-tabs nav-bordered mb-3 " style="width: 100%;">
							<li class="nav-item">
								<a href="#app-ini" data-toggle="tab" aria-expanded="false" class="nav-link active">
									<span class="d-lg-block">基本设置</span>
								</a>
							</li>
							<li class="nav-item">
								<a href="#pay-ini" data-toggle="tab" aria-expanded="true" class="nav-link">
									<span class="d-lg-block">支付设置</span>
								</a>
							</li>
							<li class="nav-item">
								<a href="#sms-ini" data-toggle="tab" aria-expanded="false" class="nav-link">
									<span class="d-lg-block">验证码设置</span>
								</a>
							</li>
							<li class="nav-item">
								<a href="#safe-ini" data-toggle="tab" aria-expanded="false" class="nav-link">
									<span class="d-lg-block">安全设置</span>
								</a>
							</li>
							<li class="nav-item">
								<a href="#bb-ini" data-toggle="tab" aria-expanded="false" class="nav-link">
									<span class="d-lg-block">版本设置</span>
								</a>
							</li>
						</ul>
					</nav>
					
					<div class="tab-content">
						<div class="tab-pane show active" id="app-ini">
							<div class="eruyi-checkbox">
								<input type="checkbox" id="state" <?php if($res['state']=='y'):?>checked<?php endif; ?> data-switch="success" onchange="state_v(this.checked)"/>
								<label for="state" data-on-label="开启" data-off-label="关闭" ></label>
								<label class="eruyi-label">应用控制</label>
							</div>
							
							<div class="view" name="state_y" id="state_y" <?php if($res['state']=='y'):?> style="display: block" <?php endif; ?>>
								<p class="text-muted">
									开启应用控制后，该应用下的用户可以 <code>正常使用</code>
								</p> 
								<div class="form-row">
									<div class="form-group col-md-12">
										<label>应用名称</label>
										<input type="text" class="form-control" id="name" placeholder="应用名称" value="<?php echo $res['name'];?>" required disabled>
									</div>
									<div class="form-group col-md-4">
										<label>APPID</label>
										<div class="input-group">
											<input type="text" class="form-control" id="appid" value="<?php echo $id;?>" disabled>
											<div class="input-group-append">
												<button class="btn btn-success" type="button" id="copy_id">复制</button>
											</div>
										</div>
									</div>
									<div class="form-group col-md-8">
										<label>APPKEY</label>
										<div class="input-group">
											<input type="text" class="form-control" id="appkey" value="<?php echo $res['appkey'];?>" disabled>
											<div class="input-group-append">
												<button class="btn btn-dark eruyi-append" type="button" id="bian_key">更换</button>
												<button class="btn btn-success" type="button" id="copy_key">复制</button>
											</div>
										</div>	
									</div>
									<div class="form-group col-md-12">
										<label>运营模式</label>
										<select class="form-control" id="mode">
											<option value="y" <?php if($res['mode']=='y') echo 'selected = "selected"'; ?>>收费模式</option>
											<option value="n" <?php if($res['mode']=='n') echo 'selected = "selected"'; ?>>免费模式</option>
										</select>	
									</div>
								</div>
							</div>	
							<div class="view" id="state_n" <?php if($res['state']=='n'):?> style="display: block" <?php endif; ?>>
								<p class="text-muted">
									关闭应用控制后，该应用下的用户 <code>不允许任何操作</code>
								</p>
								<div class="form-group">
									<label>应用关闭通知</label>
									<input type="text" id="notice" class="form-control" placeholder="告诉用户为什么关闭应用" value="<?php echo $res['notice']; ?>">
								</div>
							</div>
							
							
							<div class="eruyi-fgx-z">
								<div class="eruyi-fgx-x"></div>
								<div class="eruyi-fgx-s"></div>
							</div>
							
							<div class="eruyi-checkbox">
								<input type="checkbox" id="reg_state" <?php if($res['reg_state']=='y'):?>checked<?php endif; ?> data-switch="success" onchange="reg_state_v(this.checked)"/>
								<label for="reg_state" data-on-label="开启" data-off-label="关闭" ></label>
								<label class="eruyi-label">注册控制</label>
							</div>
							<div class="view" id="reg_state_y" <?php if($res['reg_state']=='y'):?> style="display: block" <?php endif; ?>>
								<p class="text-muted">
									开启注册后，该应用可以 <code>正常注册</code> 
								</p>
								
								<div class="form-row">
    								<div class="form-group col-md-12">
    									<label class="col-form-label">注册方式</label>
    									<select class="form-control" id="logon_way">
    										<option value="0" <?php if($res['logon_way'] == 0) echo 'selected = "selected"'; ?>>帐号</option>
    										<option value="1" <?php if($res['logon_way'] == 1) echo 'selected = "selected"'; ?>>机器码</option>
    										<option value="2" <?php if($res['logon_way'] == 2) echo 'selected = "selected"'; ?>>邀请码</option>
    										<option value="3" <?php if($res['logon_way'] == 3) echo 'selected = "selected"'; ?>>MAC地址</option>
    									</select>	
    								</div>
							    </div>
							    
								<div class="form-row">
									<div class="form-group col-md-6">
										<label>IP重复注册间隔</label>
										<div class="input-group">
											<input type="number" id="reg_ipon" class="form-control" placeholder="设置IP重复注册间隔时间" value="<?php echo $res['reg_ipon']; ?>">
											<div class="input-group-prepend">
												<span class="input-group-text">小时</span>
											</div>
										</div>
										
									</div>
									<div class="form-group col-md-6">
										<label>设备重复注册间隔</label>
										<div class="input-group">
											<input type="number" id="reg_inon" class="form-control" placeholder="设置设备重复注册间隔时间" value="<?php echo $res['reg_inon']; ?>">
											<div class="input-group-prepend">
												<span class="input-group-text">小时</span>
											</div>
										</div>
										
									</div>
								</div>
								<div class="form-row">
									<div class="form-group col-md-4">
										<label>注册奖励类型</label>
										<select class="form-control" id="reg_award" onchange="reg_change()">
											<option value="vip" <?php if($res['reg_award']=='vip') echo 'selected = "selected"'; ?>>会员</option>
											<option value="fen" <?php if($res['reg_award']=='fen') echo 'selected = "selected"'; ?>>积分</option>
										</select>
									</div>
									<div class="form-group col-md-8">
										<label>注册奖励数</label>
										<div class="input-group">
											<input type="number" id="reg_award_num" class="form-control" placeholder="0则不奖励" value="<?php echo $res['reg_award_num']; ?>">
											<div class="input-group-prepend">
												<span class="input-group-text" id="reg_award_a"><?php if($res['reg_award']=='vip'):?>分钟<?php else: ?>积分<?php endif; ?></span>
											</div>
										</div>
									</div>
								</div>
								<div class="form-row">
									<div class="form-group col-md-4">
										<label>邀请奖励类型</label>
										<select class="form-control" id="inv_award" onchange="inv_change()">
											<option value="vip" <?php if($res['inv_award']=='vip') echo 'selected = "selected"'; ?>>会员</option>
											<option value="fen" <?php if($res['inv_award']=='fen') echo 'selected = "selected"'; ?>>积分</option>
										</select>
									</div>
									<div class="form-group col-md-8">
										<label>邀请奖励数(非定制无效)</label>
										<div class="input-group">
											<input type="number" id="inv_award_num" class="form-control" placeholder="0则不奖励" value="<?php echo $res['inv_award_num']; ?>">
											<div class="input-group-prepend">
												<span class="input-group-text" id="inv_award_a"><?php if($res['inv_award']=='vip'):?>小时<?php else: ?>积分<?php endif; ?></span>
											</div>
										</div>
									</div>
								</div>
							</div>
							<div class="view" name="reg_state_n" id="reg_state_n" <?php if($res['reg_state']=='n'):?> style="display: block" <?php endif; ?>>
								<p class="text-muted">
									关闭注册后，该应用 <code>禁止所有用户注册</code>
								</p>
								<div class="form-group">
									<label>注册关闭提示</label>
									<input type="text" id="reg_notice" class="form-control" placeholder="告诉用户为什么关闭注册" value="<?php echo $res['reg_notice']; ?>">
								</div>
							</div>
							
							<div class="eruyi-fgx-z">
								<div class="eruyi-fgx-x"></div>
								<div class="eruyi-fgx-s"></div>
							</div>
							
							<div class="eruyi-checkbox">
								<input type="checkbox" id="logon_state" <?php if($res['logon_state']=='y'):?>checked<?php endif; ?> data-switch="success" onchange="logon_state_v(this.checked)"/>
								<label for="logon_state" data-on-label="开启" data-off-label="关闭" ></label>
								<label class="eruyi-label">登录控制</label>
							</div>
							
							<div class="view" id="logon_state_y" <?php if($res['logon_state']=='y'):?> style="display: block" <?php endif; ?>>
								<p class="text-muted">
									开启登录后，该应用下的用户可以 <code>正常登录</code> 使用软件（被禁封的用户除外）
								</p>
								<div class="form-row">
									<div class="form-group col-md-4">
										<label>登录时验证设备信息</label>
										<select class="form-control" id="logon_check_in" onchange="logon_check_in_v(this.value)">
											<option value="y" <?php if($res['logon_check_in']=='y') echo 'selected = "selected"'; ?>>验证</option>
											<option value="n" <?php if($res['logon_check_in']=='n') echo 'selected = "selected"'; ?>>不验证</option>
										</select>
									</div>
									<div class="form-group col-md-4">
										<label>多设备登录数</label>
										<input type="number" id="logon_num" class="form-control" placeholder="0或1则只允许同时登录一个设备" <?php if($res['logon_check_in']=='y'):?> disabled value="1" <?php elseif($res['logon_check_in']=='n'):?> value="<?php echo $res['logon_num']; ?>"<?php endif; ?> >
									</div>
									<div class="form-group col-md-4">
										<label>设备换绑间隔时间</label>
										<div class="input-group">
											<input type="number" id="logon_check_t" class="form-control" placeholder="0则不限制换绑间隔" value="<?php echo $res['logon_check_t']; ?>">
											<div class="input-group-prepend">
												<span class="input-group-text">小时</span>
											</div>
										</div>
									</div>
								</div>
								
								<div class="form-row">
									<div class="form-group col-md-4">
										<label>签到奖励类型</label>
										<select class="form-control" id="diary_award" onchange="diary_change()">
											<option value="vip" <?php if($res['diary_award']=='vip') echo 'selected = "selected"'; ?>>会员</option>
											<option value="fen" <?php if($res['diary_award']=='fen') echo 'selected = "selected"'; ?>>积分</option>
										</select>
									</div>
									<div class="form-group col-md-8">
										<label>签到奖励数</label>
										<div class="input-group">
											<input type="number" id="diary_award_num" class="form-control" placeholder="0则不奖励" value="<?php echo $res['diary_award_num']; ?>">
											<div class="input-group-prepend">
												<span class="input-group-text" id="diary_award_a"><?php if($res['diary_award']=='vip'):?>分钟<?php else: ?>积分<?php endif; ?></span>
											</div>
										</div>
									</div>
								</div>
								
							</div>
							<div class="view" id="logon_state_n" <?php if($res['logon_state']=='n'):?> style="display: block" <?php endif; ?>>
								<p class="text-muted">
									关闭登录后 <code>所有用户</code> 都无法登录该应用了
								</p>
								<div class="form-group">
									<label>登录关闭提示</label>
									<input type="text" id="logon_notice" class="form-control" placeholder="告诉用户为什么关闭登录" value="<?php echo $res['logon_notice']; ?>">
								</div>
							</div>
							
						</div>
						<div class="tab-pane" id="pay-ini">
							<div class="eruyi-checkbox">
								<input type="checkbox" id="pay_ali_state" <?php if($res['pay_ali_state']=='y'):?>checked<?php endif; ?> data-switch="info" onchange="pay_ali_state_v(this.checked)"/>
								<label for="pay_ali_state" data-on-label="开启" data-off-label="关闭" ></label>
								<label class="eruyi-label">支付宝</label>
							</div>
							<div class="view" id="pay_ali_state_y" <?php if($res['pay_ali_state']=='y'):?> style="display: block" <?php endif; ?>>
								<p class="text-muted">
									开启支付后可接入所有<code>易支付</code>平台, 只需要简单填写信息即可完成无缝对接<code>支付充值</code> 购买会员用户组
								</p>
								<div class="form-group">
									<label>支付方式</label>
									<select class="form-control" id="pay_ali_type" onchange="pay_ali_type_v(this.value)">
										<option value="0" <?php if($res['pay_ali_type']==0) echo 'selected = "selected"'; ?>>易支付</option>
										<option value="1" <?php if($res['pay_ali_type']==1) echo 'selected = "selected"'; ?>>官方</option>
										
									</select>
								</div>
								<div class="view" id="pay_ali_type_0" <?php if($res['pay_ali_type']==0):?> style="display: block" <?php endif; ?>>
									<div class="form-group">
										<label>请求地址</label>
										<div class="input-group">
											<input type="text" class="form-control" id="pay_ali_eurl" placeholder="支持所有易支付平台，域名网址" value="<?php echo $res['pay_ali_eurl'];?>">
											<div class="input-group-append">
												<button class="btn btn-dark" type="button" id="epay_ali">商户申请</button>
											</div>
										</div>
									</div>
									<div class="form-row">
										<div class="form-group col-md-4">
											<label>商户ID</label>
											<input id="pay_ali_eid" type="text" class="form-control" placeholder="商户ID" value="<?php echo $res['pay_ali_eid']; ?>">
										</div>
										<div class="form-group col-md-8">
											<label>商户KEY</label>
											<input id="pay_ali_ekey" type="text" class="form-control" placeholder="商户KEY" value="<?php echo $res['pay_ali_ekey']; ?>">
										</div>
										<div class="form-group col-md-12">
										    <label>异步通知地址</label>
    										<div class="input-group">
            									<input id="pay_ali_notify" type="text" class="form-control"   placeholder="异步通知地址" value="<?php if($res['pay_ali_notify']==''){echo dirname(WEB_URL).'/ali_notify.php';}else{echo $res['pay_ali_notify'];}?>">
            								</div>
        								</div>
									</div>
								</div>
								<div class="view" id="pay_ali_type_1" <?php if($res['pay_ali_type']==1):?> style="display: block" <?php endif; ?>>
									<div class="form-group">
										<label>APPID</label>
										<input type="text" class="form-control" id="pay_ali_appid" placeholder="支付宝 APPID" value="<?php echo $res['pay_ali_appid'];?>">
									</div>
									
									<div class="form-group">
										<label>支付宝公钥</label>
										<input id="pay_ali_public_key" type="text" class="form-control" placeholder="支付宝公钥" value="<?php echo $res['pay_ali_public_key']; ?>">
									</div>
									<div class="form-group">
										<label>商户私钥</label>
										<input id="pay_ali_private_key" type="text" class="form-control" placeholder="商户私钥" value="<?php echo $res['pay_ali_private_key']; ?>">
									</div>
								</div>
								
							</div>
							<div class="view" id="pay_ali_state_n" <?php if($res['pay_ali_state']=='n'):?> style="display: block" <?php endif; ?>>
								<p class="text-muted">
									关闭支付宝后该应用则<code>无法使用支付宝</code>方式进行支付
								</p>
							</div>
							
							<div class="eruyi-fgx-z">
								<div class="eruyi-fgx-x"></div>
								<div class="eruyi-fgx-s"></div>
							</div>
							
							<div class="eruyi-checkbox">
								<input type="checkbox" id="pay_wx_state" <?php if($res['pay_wx_state']=='y'):?>checked<?php endif; ?> data-switch="success" onchange="pay_wx_state_v(this.checked)"/>
								<label for="pay_wx_state" data-on-label="开启" data-off-label="关闭" ></label>
								<label class="eruyi-label">微信</label>
							</div>
							<div class="view" id="pay_wx_state_y" <?php if($res['pay_wx_state']=='y'):?> style="display: block" <?php endif; ?>>
								<p class="text-muted">
									开启支付后可接入所有<code>易支付</code>平台, 只需要简单填写信息即可完成无缝对接<code>支付充值</code> 购买会员用户组
								</p>
								<div class="form-group">
									<label>支付方式</label>
									<select class="form-control" id="pay_wx_type" onchange="pay_wx_type_v(this.value)">
										<option value="0" <?php if($res['pay_wx_type']==0) echo 'selected = "selected"'; ?>>易支付</option>
										<option value="1" <?php if($res['pay_wx_type']==1) echo 'selected = "selected"'; ?>>官方</option>
										
									</select>
								</div>
								<div class="view" id="pay_wx_type_0" <?php if($res['pay_wx_type']==0):?> style="display: block" <?php endif; ?>>
									<div class="form-group">
										<label>请求地址</label>
										<div class="input-group">
											<input type="text" class="form-control" id="pay_wx_eurl" placeholder="支持所有易支付平台，域名网址" value="<?php echo $res['pay_wx_eurl'];?>">
											<div class="input-group-append">
												<button class="btn btn-dark" type="button" id="epay_wx">商户申请</button>
											</div>
										</div>
									</div>
									<div class="form-row">
										<div class="form-group col-md-4">
											<label>商户ID</label>
											<input id="pay_wx_eid" type="text" class="form-control" placeholder="商户ID" value="<?php echo $res['pay_wx_eid']; ?>">
										</div>
										<div class="form-group col-md-8">
											<label>商户KEY</label>
											<input id="pay_wx_ekey" type="text" class="form-control" placeholder="商户KEY" value="<?php echo $res['pay_wx_ekey']; ?>">
										</div>
										<div class="form-group col-md-12">
										    <label>异步通知地址</label>
    										<div class="input-group">
            									<input id="pay_wx_notify" type="text" class="form-control"   placeholder="异步通知地址" value="<?php if($res['pay_wx_notify']==''){echo dirname(WEB_URL).'/wx_notify.php';}else{echo $res['pay_wx_notify'];}?>">
            								</div>
        								</div>
									</div>
								</div>
								
								<div class="view" id="pay_wx_type_1" <?php if($res['pay_wx_type']==1):?> style="display: block" <?php endif; ?>>
									<div class="form-group">
										<label>微信支付APPID</label>
										<input type="text" class="form-control" id="pay_wx_appid" placeholder="微信公众号APPID" value="<?php echo $res['pay_wx_appid'];?>">
									</div>
									
									<div class="form-group">
										<label>微信支付MCHID</label>
										<input id="pay_wx_mchid" type="text" class="form-control" placeholder="微信支付商户ID" value="<?php echo $res['pay_wx_mchid']; ?>">
									</div>
									<div class="form-group">
										<label>微信支付KEY</label>
										<input id="pay_wx_key" type="text" class="form-control" placeholder="微信支付KEY" value="<?php echo $res['pay_wx_key']; ?>">
									</div>
									<div class="form-group">
										<label>微信支付APPSECRET</label>
										<input id="pay_wx_appsecret" type="text" class="form-control" placeholder="微信支付APPSECRET" value="<?php echo $res['pay_wx_appsecret']; ?>">
									</div>
								</div>
								
							</div>
							<div class="view" id="pay_wx_state_n" <?php if($res['pay_wx_state']=='n'):?> style="display: block" <?php endif; ?>>
								<p class="text-muted">
									关闭支付后该应用则<code>无法使用</code>支付功能
								</p>
							</div>
							
							<div class="eruyi-fgx-z">
								<div class="eruyi-fgx-x"></div>
								<div class="eruyi-fgx-s"></div>
							</div>
							
							<div class="eruyi-checkbox">
								<input type="checkbox" id="pay_qq_state" <?php if($res['pay_qq_state']=='y'):?>checked<?php endif; ?> data-switch="secondary" onchange="pay_qq_state_v(this.checked)"/>
								<label for="pay_qq_state" data-on-label="开启" data-off-label="关闭" ></label>
								<label class="eruyi-label">QQ钱包</label>
							</div>
							<div class="view" id="pay_qq_state_y" <?php if($res['pay_qq_state']=='y'):?> style="display: block" <?php endif; ?>>
								<p class="text-muted">
									开启支付后可接入所有<code>易支付</code>平台, 只需要简单填写信息即可完成无缝对接<code>支付充值</code> 购买会员用户组
								</p>
								
								<div class="form-group">
									<label>支付方式</label>
									<select class="form-control" id="pay_qq_type" onchange="pay_qq_type_v(this.value)">
										<option value="0" <?php if($res['pay_qq_type']==0) echo 'selected = "selected"'; ?>>易支付</option>
										<option value="1" <?php if($res['pay_qq_type']==1) echo 'selected = "selected"'; ?>>官方</option>
										
									</select>
								</div>
								<div class="view" id="pay_qq_type_0" <?php if($res['pay_qq_type']==0):?> style="display: block" <?php endif; ?>>
									<div class="form-group">
										<label>请求地址</label>
										<div class="input-group">
											<input type="text" class="form-control" id="pay_qq_eurl" placeholder="支持所有易支付平台，域名网址" value="<?php echo $res['pay_qq_eurl'];?>">
											<div class="input-group-append">
												<button class="btn btn-dark" type="button" id="epay_qq">商户申请</button>
											</div>
										</div>
									</div>
									<div class="form-row">
										<div class="form-group  col-md-4">
											<label>商户ID</label>
											<input id="pay_qq_eid" type="text" class="form-control" placeholder="商户ID" value="<?php echo $res['pay_qq_eid']; ?>">
										</div>
										<div class="form-group  col-md-8">
											<label>商户KEY</label>
											<input id="pay_qq_ekey" type="text" class="form-control" placeholder="商户KEY" value="<?php echo $res['pay_qq_ekey']; ?>">
										</div>
										<div class="form-group col-md-12">
										    <label>异步通知地址</label>
    										<div class="input-group">
            									<input id="pay_qq_notify" type="text" class="form-control"   placeholder="异步通知地址" value="<?php if($res['pay_qq_notify']==''){echo dirname(WEB_URL).'/qq_notify.php';}else{echo $res['pay_qq_notify'];}?>">
            								</div>
        								</div>
									</div>
								</div>
								
								<div class="view" id="pay_qq_type_1" <?php if($res['pay_qq_type']==1):?> style="display: block" <?php endif; ?>>
									<div class="form-group">
										<label>QQ钱包MCHID</label>
										<input id="pay_qq_mchid" type="text" class="form-control" placeholder="QQ钱包MCHID" value="<?php echo $res['pay_qq_mchid']; ?>">
									</div>
									<div class="form-group">
										<label>QQ钱包MCHKEY</label>
										<input id="pay_qq_mchkey" type="text" class="form-control" placeholder="QQ钱包MCHKEY" value="<?php echo $res['pay_qq_mchkey']; ?>">
									</div>
								</div>
								
							</div>
							<div class="view" id="pay_qq_state_n" <?php if($res['pay_qq_state']=='n'):?> style="display: block" <?php endif; ?>>
								<p class="text-muted">
									关闭支付后该应用则<code>无法使用</code>支付功能
								</p>
							</div>
						</div>
						
						<div class="tab-pane" id="sms-ini">
							<div class="eruyi-checkbox">
								<input type="checkbox" id="smtp_state" <?php if($res['smtp_state']=='y'):?>checked<?php endif; ?> data-switch="secondary" onchange="smtp_state_v(this.checked)"/>
								<label for="smtp_state" data-on-label="开启" data-off-label="关闭" ></label>
								<label class="eruyi-label">邮箱控制</label>
							</div>
							
							<div class="view" id="smtp_state_y" <?php if($res['smtp_state']=='y'):?> style="display: block" <?php endif; ?>>
								<p class="text-muted">
									开启邮箱控制后，用户 <code>可以使用邮箱获取验证码</code> 注册和找回密码
								</p>
								
								<div class="form-row">
									<div class="form-group  col-md-6">
										<label>SMTP服务器</label>
										<input id="smtp_host" type="text" class="form-control" placeholder="邮箱服务器" value="<?php echo $res['smtp_host'];?>">
									</div>
									<div class="form-group  col-md-6">
										<label>端口</label>
										<input id="smtp_port" type="number" class="form-control" placeholder="邮箱端口" value="<?php echo $res['smtp_port'];?>">
									</div>
								</div>
								<div class="form-row">
									<div class="form-group  col-md-6">
										<label>SMTP用户名</label>
										<input id="smtp_user" type="text" class="form-control" placeholder="邮箱账号" value="<?php echo $res['smtp_user'];?>">
									</div>
									<div class="form-group  col-md-6">
										<label>SMTP密码</label>
										<input id="smtp_pass" type="text" class="form-control" placeholder="邮箱密码" value="<?php echo $res['smtp_pass'];?>">
									</div>
								</div>
							</div>
							<div class="view" id="smtp_state_n" <?php if($res['smtp_state']=='n'):?> style="display: block" <?php endif; ?>>
								<p class="text-muted">
									关闭邮箱控制后，用户 <code>无法使用</code> 邮箱注册验证码和邮箱找回密码
								</p>
							</div>
							
							<div class="eruyi-fgx-z">
								<div class="eruyi-fgx-x"></div>
								<div class="eruyi-fgx-s"></div>
							</div>
							
							
							<div class="eruyi-checkbox">
								<input type="checkbox" id="sms_state" <?php if($res['sms_state']=='y'):?>checked<?php endif; ?> data-switch="bool" onchange="sms_state_v(this.checked)"/>
								<label for="sms_state" data-on-label="开启" data-off-label="关闭" ></label>
								<label class="eruyi-label">短信控制</label>
							</div>
							
							<div class="view" id="sms_state_y" <?php if($res['sms_state']=='y'):?> style="display: block" <?php endif; ?>>
								<p class="text-muted">
									开启短信控制后，用户 <code>可以使用短信获取验证码</code> 注册和找回密码
								</p>
								<div class="form-row">
									<div class="form-group  col-md-12">
										<label>短信KEY</label>
										<input id="sms_key" type="text" class="form-control" placeholder="短信APPKEY" value="<?php echo $res['sms_key'];?>">
									</div>
								</div>
							</div>
							<div class="view" id="sms_state_n" <?php if($res['sms_state']=='n'):?> style="display: block" <?php endif; ?>>
								<p class="text-muted">
									关闭短信控制后，用户 <code>无法使用</code> 短信注册验证码和短信找回密码
								</p>
							</div>
						</div>
						
						<div class="tab-pane" id="safe-ini">
							<div class="eruyi-checkbox">
								<input type="checkbox" id="mi_state" <?php if($res['mi_state']=='y'):?>checked<?php endif; ?> data-switch="warning" onchange="mi_state_v(this.checked)"/>
								<label for="mi_state" data-on-label="开启" data-off-label="关闭" ></label>
								<label class="eruyi-label">安全控制</label>
							</div>
							<div class="view" id="mi_state_y" <?php if($res['mi_state']=='y'):?> style="display: block" <?php endif; ?>>
								<p class="text-muted">
									开启安全控制后，可对应用 <code>数据</code> 进行加密, 防止数据泄露
								</p>
								<div class="form-group">
									<label>数据加密类型</label>
									<select class="form-control" id="mi_type" onchange="mi_type_v(this.value)">
										<option value="0" <?php if($res['mi_type']==0) echo 'selected = "selected"'; ?>>不加密</option>
										<option value="1" <?php if($res['mi_type']==1) echo 'selected = "selected"'; ?>>RC4加密</option>
										<option value="2" <?php if($res['mi_type']==2) echo 'selected = "selected"'; ?>>RSA加密</option>
										<option value="3" <?php if($res['mi_type']==3) echo 'selected = "selected"'; ?>>AES加密</option>
									</select>
								</div>
								<div class="view" id="mi_type_0" <?php if($res['mi_type']==0):?> style="display: block" <?php endif; ?>>
									<div class="alert alert-warning alert-dismissible fade show" role="alert">
										<button type="button" class="close" data-dismiss="alert" aria-label="Close">
											<span aria-hidden="true">&times;</span>
										</button>
										<strong>提示 - </strong> 该设置仅针对数据加密，不影响其他安全设置
									</div>
								</div>
								
								<div class="view" id="mi_type_1" <?php if($res['mi_type']==1):?> style="display: block" <?php endif; ?>>
									<div class="form-group">
										<label>RC4秘钥</label>
										<div class="input-group">
											<input type="text" id="mi_rc4_key" class="form-control" placeholder="RC4加解密秘钥" value="<?php echo $res['mi_rc4_key']; ?>">
											<div class="input-group-append">
												<button class="btn btn-dark" type="button" id="mi_skey">随机</button>
											</div>
										</div>
									</div>
								</div>
								
								<div class="view" id="mi_type_2" <?php if($res['mi_type']==2):?> style="display: block" <?php endif; ?>>
									<div class="form-group">
										<label>私钥</label>
										<textarea class="form-control" id="mi_rsa_private_key" rows="5" placeholder="加解密私钥"><?php echo $res['mi_rsa_private_key']; ?></textarea>
									</div>
									<div class="form-group">
										<label>公钥</label>
										<textarea class="form-control" id="mi_rsa_public_key" rows="5" placeholder="加解密公钥"><?php echo $res['mi_rsa_public_key']; ?></textarea>
									</div>
								</div>
								
								<div class="view" id="mi_type_3" <?php if($res['mi_type']==3):?> style="display: block" <?php endif; ?>>
									<!-- <div class="form-group">
										<label>AES密钥</label>
										<textarea class="form-control" id="mi_aes_key" rows="1" placeholder="密钥长度必须为32"><?php echo $res['mi_aes_key']; ?></textarea>
									</div>
									<div class="form-group">
										<label>AESIV</label>
										<textarea class="form-control" id="mi_aes_iv" rows="1" placeholder="IV长度必须为16"><?php echo $res['mi_aes_iv']; ?></textarea>
									</div> -->
									<label>AES密钥</label>
									<div class="input-group">
										<input type="text" id="mi_aes_key" class="form-control" placeholder="密钥长度必须为32" value="<?php echo $res['mi_aes_key']; ?>">
										<div class="input-group-append">
											<button class="btn btn-dark" type="button" id="mi_skey_aes">随机</button>
										</div>
									</div>
									
									<label>AES密钥</label>
									<div class="input-group">
										<input type="text" id="mi_aes_iv" class="form-control" placeholder="IV长度必须为16" value="<?php echo $res['mi_aes_iv']; ?>">
									</div>
								</div>
								
								<div class="form-group">
									<label>数据签名</label>
									<select class="form-control" id="mi_sign">
										<option value="n" <?php if($res['mi_sign']=='n') echo 'selected = "selected"'; ?>>不签名</option>
										<option value="y" <?php if($res['mi_sign']=='y') echo 'selected = "selected"'; ?>>签名</option>
									</select>
								</div>
								<div class="alert alert-warning alert-dismissible fade show" role="alert">
									<button type="button" class="close" data-dismiss="alert" aria-label="Close">
										<span aria-hidden="true">&times;</span>
									</button>
									<strong>提示 - </strong> 若使用数据签名，可有效防止数据被篡改
								</div>
								<div class="form-group">
									<label>时间差校验</label>
									<div class="input-group">
										<input id="mi_time" type="number" class="form-control" placeholder="时间校验" value="<?php echo $res['mi_time']; ?>">
										<div class="input-group-prepend">
											<span class="input-group-text">秒</span>
										</div>
									</div>
								</div>
								<div class="alert alert-warning alert-dismissible fade show" role="alert">
									<button type="button" class="close" data-dismiss="alert" aria-label="Close">
										<span aria-hidden="true">&times;</span>
									</button>
									<strong>提示 - </strong> 对客户设备时间与服务器时间进行时差校验，避免用户修改本地时间非法使用VIP功能，设置 0 则不校验
								</div>
							</div>
							<div class="view" id="mi_state_n" <?php if($res['mi_state']=='n'):?> style="display: block" <?php endif; ?>>
								<p class="text-muted">
									关闭安全控制后，该应用 <code>数据</code> 将以明文传输，不使用任何安全配置
								</p>
							</div>
						</div>
						
						<div class="tab-pane" id="bb-ini">
							<div class="eruyi-checkbox">
								<input type="checkbox" id="android_state" <?php if($res['android_state']=='y'):?>checked<?php endif; ?> data-switch="success" onchange="android_v(this.checked)"/>
								<label for="android_state" data-on-label="开启" data-off-label="关闭" ></label>
								<label class="eruyi-label">神马版本</label>
							</div>

							<div class="view" id="android_y" <?php if($res['android_state']=='y'):?> style="display: block" <?php endif; ?>>
								<p class="text-muted">
									开启神马版本后，可根据接口获取 <code>神马版本配置</code>
								</p>
								
								<div class="form-row">
									<div class="form-group col-md-2">
										<label>应用版本</label>
										<input type="number" id="android_bb" class="form-control" placeholder="1.0" value="<?php echo $res['android_bb']; ?>">
										
									</div>
									
									<div class="form-group col-md-1">
										<label>下载类型</label>
										<select class="form-control" id="downloadtype1" onchange="logon_check_in_a(this.value)">
											<option value="0" <?php if($res['downloadtype1']=='0') echo 'selected = "selected"'; ?>>直连</option>
											<option value="1" <?php if($res['downloadtype1']=='1') echo 'selected = "selected"'; ?>>蓝奏云</option>
											<option value="2" <?php if($res['downloadtype1']=='2') echo 'selected = "selected"'; ?>>乐家市场</option>
										</select>
									</div>
									
									<div class="form-group col-md-1">
										<label>密码</label>
										<input type="text" id="downloadpwd1" class="form-control" placeholder="无密码留空" <?php if($res['downloadtype1']=='0'):?> disabled value="<?php echo $res['downloadpwd1']; ?>" <?php elseif($res['downloadtype1']=='1'):?> value="<?php echo $res['downloadpwd1']; ?>"<?php endif; ?> >
									</div>
													
									<div class="form-group col-md-8">
										<label>更新地址</label>
										<div class="input-group">
											<input type="text" id="android_url" class="form-control" placeholder="版本更新地址" value="<?php echo $res['android_url']; ?>">
											<div class="input-group-append">
												<button type="button" class="btn btn-primary" onclick="document.getElementById('apkfile').click()">📤 上传APK</button>
											</div>
										</div>
										<input type="file" id="apkfile" accept=".apk" style="display:none" onchange="uploadApk(this)">
										<div id="upload_progress" style="display:none;margin-top:5px;">
											<div style="font-size:12px;color:#888;" id="upload_status">准备上传...</div>
											<div style="background:#eee;border-radius:4px;height:8px;margin-top:3px;overflow:hidden;">
												<div id="upload_bar" style="width:0%;height:100%;background:#007bff;transition:width 0.3s;"></div>
											</div>
										</div>
									</div>
									
								</div>
								<div class="form-row">
									<div class="form-group col-md-12">
										<label>更新内容</label>
										<textarea class="form-control" id="android_show" rows="5" placeholder="版本更新内容"><?php echo $res['android_show']; ?></textarea>
									</div>
								</div>	
								
							</div>	
							<div class="view" id="android_n" <?php if($res['android_state']=='n'):?> style="display: block" <?php endif; ?>>
								<p class="text-muted">
									关闭神马版本后，则无法根据接口获取 <code>神马版本配置</code>
								</p>
							</div>
							
							<div class="eruyi-fgx-z">
								<div class="eruyi-fgx-x"></div>
								<div class="eruyi-fgx-s"></div>
							</div>
							
							<div class="eruyi-checkbox">
								<input type="checkbox" id="ios_state" <?php if($res['ios_state']=='y'):?>checked<?php endif; ?> data-switch="success" onchange="ios_v(this.checked)"/>
								<label for="ios_state" data-on-label="开启" data-off-label="关闭" ></label>
								<label class="eruyi-label">293版本</label>
							</div>

							<div class="view" id="ios_y" <?php if($res['ios_state']=='y'):?> style="display: block" <?php endif; ?>>
								<p class="text-muted">
									开启293版本后，可根据接口获取 <code>293版本配置</code>
								</p>
								
								<div class="form-row">
									<div class="form-group col-md-2">
										<label>应用版本</label>
										<input type="number" id="ios_bb" class="form-control" placeholder="1.0" value="<?php echo $res['ios_bb']; ?>">
										
									</div>
									
									<div class="form-group col-md-1">
										<label>下载类型</label>
										<select class="form-control" id="downloadtype2" onchange="logon_check_in_b(this.value)">
											<option value="0" <?php if($res['downloadtype2']=='0') echo 'selected = "selected"'; ?>>直连</option>
											<option value="1" <?php if($res['downloadtype2']=='1') echo 'selected = "selected"'; ?>>蓝奏云</option>
											<option value="2" <?php if($res['downloadtype2']=='2') echo 'selected = "selected"'; ?>>乐家市场</option>
										</select>
									</div>
									
									<div class="form-group col-md-1">
										<label>密码</label>
										<input type="text" id="downloadpwd2" class="form-control" placeholder="无密码留空" <?php if($res['downloadtype2']=='0'):?> disabled value="<?php echo $res['downloadpwd2']; ?>" <?php elseif($res['downloadtype2']=='1'):?> value="<?php echo $res['downloadpwd2']; ?>"<?php endif; ?> >
									</div>
									
									<div class="form-group col-md-8">
										<label>更新地址</label>
										<input type="text" id="ios_url" class="form-control" placeholder="版本更新地址" value="<?php echo $res['ios_url']; ?>">
									</div>
									
								</div>
								<div class="form-row">
									<div class="form-group col-md-12">
										<label>更新内容</label>
										<textarea class="form-control" id="ios_show" rows="5" placeholder="版本更新内容"><?php echo $res['ios_show']; ?></textarea>
									</div>
								</div>	
								
							</div>	
							<div class="view" id="ios_n" <?php if($res['ios_state']=='n'):?> style="display: block" <?php endif; ?>>
								<p class="text-muted">
									关闭293版本后，则无法根据接口获取 <code>293版本配置</code>
								</p>
							</div>
						</div>
					</div>
				</div> <!-- end card-body-->
			</div> <!-- end card-->
		</div> <!-- end col -->
	</div>
	<!-- end row-->
	
	
	<!-- Form row -->
	<div class="row">
		<div class="col-12">
			<div class="card">
				<div class="card-body">
					<button type="submit" class="btn btn-block btn-primary" id="submit">确认修改</button>
				</div> <!-- end card-body -->
			</div> <!-- end card-->
		</div> <!-- end col -->
	</div>
	
	<script> 
		$("#app_adm").addClass("active");
		$('#submit').click(function() {
			let t = window.jQuery;
			
			var state = document.getElementById("state").checked ? 'y':'n';
			var logon_state = document.getElementById("logon_state").checked ? 'y':'n';
			var reg_state = document.getElementById("reg_state").checked ? 'y':'n';
			var mi_state = document.getElementById("mi_state").checked ? 'y':'n';
			var smtp_state = document.getElementById("smtp_state").checked ? 'y':'n';
			var sms_state = document.getElementById("sms_state").checked ? 'y':'n';
			var pay_ali_state = document.getElementById("pay_ali_state").checked ? 'y':'n';
			var pay_wx_state = document.getElementById("pay_wx_state").checked ? 'y':'n';
			var pay_qq_state = document.getElementById("pay_qq_state").checked ? 'y':'n';
			var android_state = document.getElementById("android_state").checked ? 'y':'n';
			var ios_state = document.getElementById("ios_state").checked ? 'y':'n';
			
			var name = $("#name").val();
			var appkey = $("#appkey").val();
			var mode = $("#mode").val();
			var notice = $("#notice").val();
			
			var reg_ipon = $("#reg_ipon").val();
			var reg_inon = $("#reg_inon").val();
			var reg_award = $("#reg_award").val();
			var reg_award_num = $("#reg_award_num").val();
			var inv_award = $("#inv_award").val();
			var inv_award_num = $("#inv_award_num").val();
			var reg_notice = $("#reg_notice").val();
			
			var logon_check_in= $("#logon_check_in").val();
			var logon_check_t = $("#logon_check_t").val();
			var logon_num = $("#logon_num").val();
			var diary_award = $("#diary_award").val();
			var diary_award_num = $("#diary_award_num").val();
			var logon_notice = $("#logon_notice").val();
			
			var pay_ali_eurl = $("#pay_ali_eurl").val();
			var pay_ali_eid = $("#pay_ali_eid").val();
			var pay_ali_ekey = $("#pay_ali_ekey").val();
			var pay_ali_notify = $("#pay_ali_notify").val();
			
			var pay_wx_eurl = $("#pay_wx_eurl").val();
			var pay_wx_eid = $("#pay_wx_eid").val();
			var pay_wx_ekey = $("#pay_wx_ekey").val();
			var pay_wx_notify = $("#pay_wx_notify").val();
			
			var pay_qq_eurl = $("#pay_qq_eurl").val();
			var pay_qq_eid = $("#pay_qq_eid").val();
			var pay_qq_ekey = $("#pay_qq_ekey").val();
			var pay_qq_notify = $("#pay_qq_notify").val();
			
			var pay_ali_appid = $("#pay_ali_appid").val();
			var pay_ali_public_key = $("#pay_ali_public_key").val();
			var pay_ali_private_key = $("#pay_ali_private_key").val();
			var pay_wx_appid = $("#pay_wx_appid").val();
			var pay_wx_mchid = $("#pay_wx_mchid").val();
			var pay_wx_key = $("#pay_wx_key").val();
			var pay_wx_appsecret = $("#pay_wx_appsecret").val();
			var pay_qq_mchid = $("#pay_qq_mchid").val();
			var pay_qq_mchkey = $("#pay_qq_mchkey").val();
			
			var smtp_host = $("#smtp_host").val();
			var smtp_user = $("#smtp_user").val();
			var smtp_pass = $("#smtp_pass").val();
			var smtp_port = $("#smtp_port").val();
			
			var sms_key = $("#sms_key").val();
			
			var mi_type = $("#mi_type").val();
			var mi_sign = $("#mi_sign").val();
			var mi_time = $("#mi_time").val();
			var mi_aes_key = $("#mi_aes_key").val();
			var mi_aes_iv = $("#mi_aes_iv").val();
			var mi_rsa_private_key = $("#mi_rsa_private_key").val();
			var mi_rsa_public_key = $("#mi_rsa_public_key").val();
			var mi_rc4_key = $("#mi_rc4_key").val();
			
			var ios_bb = $("#ios_bb").val();
			var ios_url = $("#ios_url").val();
			var ios_show = $("#ios_show").val();
			var downloadtype2 = $("#downloadtype2").val();
			var downloadpwd2 = $("#downloadpwd2").val();
			
			var android_bb = $("#android_bb").val();
			var android_url = $("#android_url").val();
			var android_show = $("#android_show").val();
			var downloadtype1 = $("#downloadtype1").val();
			var downloadpwd1 = $("#downloadpwd1").val();
			
			var logon_way = $("#logon_way").val();
			
			document.getElementById('submit').innerHTML="<span class=\"spinner-border spinner-border-sm mr-1\" role=\"status\" aria-hidden=\"true\"></span>正在修改";
			document.getElementById('submit').disabled=true;
			
			$.ajax({
				cache: false,
				type: "POST",//请求的方式
				url : "ajax.php?act=app_edit",//请求的文件名
				data : {
					id:<?php echo $id;?>,
					state:state,
					logon_state:logon_state,
					reg_state:reg_state,
					mi_state:mi_state,
					smtp_state:smtp_state,
					sms_state:sms_state,
					pay_ali_state:pay_ali_state,
					pay_wx_state:pay_wx_state,
					pay_qq_state:pay_qq_state,
					android_state:android_state,
					ios_state:ios_state,
					
					name:name,
					appkey:appkey,
					mode:mode,
					notice:notice,
					
					reg_ipon:reg_ipon,
					reg_inon:reg_inon,
					reg_award:reg_award,
					reg_award_num:reg_award_num,
					inv_award:inv_award,
					inv_award_num:inv_award_num,
					reg_notice:reg_notice,
					
					logon_check_in:logon_check_in,
					logon_check_t:logon_check_t,
					logon_num:logon_num,
					diary_award:diary_award,
					diary_award_num:diary_award_num,
					logon_notice:logon_notice,
					
					pay_ali_eurl:pay_ali_eurl,
					pay_ali_eid:pay_ali_eid,
					pay_ali_ekey:pay_ali_ekey,
					pay_ali_notify:pay_ali_notify,
					
					pay_wx_eurl:pay_wx_eurl,
					pay_wx_eid:pay_wx_eid,
					pay_wx_ekey:pay_wx_ekey,
					pay_wx_notify:pay_wx_notify,
					
					pay_qq_eurl:pay_qq_eurl,
					pay_qq_eid:pay_qq_eid,
					pay_qq_ekey:pay_qq_ekey,
					pay_qq_notify:pay_qq_notify,
					
					pay_ali_appid:pay_ali_appid,
					pay_ali_public_key:pay_ali_public_key,
					pay_ali_private_key:pay_ali_private_key,
					pay_wx_appid:pay_wx_appid,
					pay_wx_mchid:pay_wx_mchid,
					pay_wx_key:pay_wx_key,
					pay_wx_appsecret:pay_wx_appsecret,
					pay_qq_mchid:pay_qq_mchid,
					pay_qq_mchkey:pay_qq_mchkey,
					
					smtp_host:smtp_host,
					smtp_user:smtp_user,
					smtp_pass:smtp_pass,
					smtp_port:smtp_port,
					
					sms_key:sms_key,
					
					mi_type:mi_type,
					mi_sign:mi_sign,
					mi_time:mi_time,
					mi_aes_key:mi_aes_key,
					mi_aes_iv:mi_aes_iv,
					mi_rsa_private_key:mi_rsa_private_key,
					mi_rsa_public_key:mi_rsa_public_key,
					mi_rc4_key:mi_rc4_key,
					
					ios_bb:ios_bb,
					ios_url:ios_url,
					ios_show:ios_show,
					downloadtype2:downloadtype2,
					downloadpwd2:downloadpwd2,
					
					android_bb:android_bb,
					android_url:android_url,
					android_show:android_show,
					downloadtype1:downloadtype1,
					downloadpwd1:downloadpwd1,
					
					logon_way:logon_way
				},
				dataType : 'json',
				success : function(data) {
					console.log(data);
					document.getElementById('submit').disabled=false;
					document.getElementById('submit').innerHTML="确认修改";
					if(data.code == 200){
						t.NotificationApp.send("成功",data.msg,"top-center","rgba(0,0,0,0.2)","success")
						//window.setTimeout("window.location='"+window.location.href+"'",1000);
					}else{
						t.NotificationApp.send("失败",data.msg,"top-center","rgba(0,0,0,0.2)","error")
					}
				}
			});
			return false;//重要语句：如果是像a链接那种有href属性注册的点击事件，可以阻止它跳转。
		});
		
		
		function mi_type_v(i) {
			if(i==0){
				$("#mi_type_0").css("display", "block");
				$("#mi_type_1").css("display", "none");
				$("#mi_type_2").css("display", "none");
				$("#mi_type_3").css("display", "none");
			}else if(i==1){
				$("#mi_type_0").css("display", "none");
				$("#mi_type_1").css("display", "block");
				$("#mi_type_2").css("display", "none");
				$("#mi_type_3").css("display", "none");
			}else if(i==2){
				$("#mi_type_0").css("display", "none");
				$("#mi_type_1").css("display", "none");
				$("#mi_type_2").css("display", "block");
				$("#mi_type_3").css("display", "none");
			}else if(i==3){
				$("#mi_type_0").css("display", "none");
				$("#mi_type_1").css("display", "none");
				$("#mi_type_2").css("display", "none");
				$("#mi_type_3").css("display", "block");
			}
		}
		
		function pay_ali_type_v(i) {
			if(i==0){
				$("#pay_ali_type_0").css("display", "block");
				$("#pay_ali_type_1").css("display", "none");
				$("#pay_ali_type_2").css("display", "none");
			}else if(i==1){
				$("#pay_ali_type_0").css("display", "none");
				$("#pay_ali_type_1").css("display", "block");
				$("#pay_ali_type_2").css("display", "none");
				
			}else if(i==2){
				$("#pay_ali_type_0").css("display", "none");
				$("#pay_ali_type_1").css("display", "none");
				$("#pay_ali_type_2").css("display", "block");
			}
		}
		
		function pay_wx_type_v(i) {
			if(i==0){
				$("#pay_wx_type_0").css("display", "block");
				$("#pay_wx_type_1").css("display", "none");
				$("#pay_wx_type_2").css("display", "none");
			}else if(i==1){
				$("#pay_wx_type_0").css("display", "none");
				$("#pay_wx_type_1").css("display", "block");
				$("#pay_wx_type_2").css("display", "none");
			}else if(i==2){
				$("#pay_wx_type_0").css("display", "none");
				$("#pay_wx_type_1").css("display", "none");
				$("#pay_wx_type_2").css("display", "block");
			}
		}
		
		function pay_qq_type_v(i) {
			if(i==0){
				$("#pay_qq_type_0").css("display", "block");
				$("#pay_qq_type_1").css("display", "none");
				$("#pay_qq_type_2").css("display", "none");
			}else if(i==1){
				$("#pay_qq_type_0").css("display", "none");
				$("#pay_qq_type_1").css("display", "block");
				$("#pay_qq_type_2").css("display", "none");
				
			}else if(i==2){
				$("#pay_qq_type_0").css("display", "none");
				$("#pay_qq_type_1").css("display", "none");
				$("#pay_qq_type_2").css("display", "block");
			}
		}
		
		function smtp_state_v(i) {
			//console.log(i);
			if(i==true){
				$("#smtp_state_y").css("display", "block");
				$("#smtp_state_n").css("display", "none");
			}else{
				$("#smtp_state_y").css("display", "none");
				$("#smtp_state_n").css("display", "block");
			}
		}
		
		function sms_state_v(i) {
			//console.log(i);
			if(i==true){
				$("#sms_state_y").css("display", "block");
				$("#sms_state_n").css("display", "none");
			}else{
				$("#sms_state_y").css("display", "none");
				$("#sms_state_n").css("display", "block");
			}
		}
		
		function mi_state_v(i) {
			//console.log(i);
			if(i==true){
				$("#mi_state_y").css("display", "block");
				$("#mi_state_n").css("display", "none");
			}else{
				$("#mi_state_y").css("display", "none");
				$("#mi_state_n").css("display", "block");
			}
		}
		function pay_ali_state_v(i) {
			//console.log(i);
			if(i==true){
				$("#pay_ali_state_y").css("display", "block");
				$("#pay_ali_state_n").css("display", "none");
			}else{
				$("#pay_ali_state_y").css("display", "none");
				$("#pay_ali_state_n").css("display", "block");
			}
		}
		function pay_wx_state_v(i) {
			//console.log(i);
			if(i==true){
				$("#pay_wx_state_y").css("display", "block");
				$("#pay_wx_state_n").css("display", "none");
			}else{
				$("#pay_wx_state_y").css("display", "none");
				$("#pay_wx_state_n").css("display", "block");
			}
		}
		function pay_qq_state_v(i) {
			//console.log(i);
			if(i==true){
				$("#pay_qq_state_y").css("display", "block");
				$("#pay_qq_state_n").css("display", "none");
			}else{
				$("#pay_qq_state_y").css("display", "none");
				$("#pay_qq_state_n").css("display", "block");
			}
		}
		
		
		function state_v(i) {
			if(i==true){
				$("#state_y").css("display", "block");
				$("#state_n").css("display", "none");
			}else{
				$("#state_y").css("display", "none");
				$("#state_n").css("display", "block");
			}
		}
		
		function logon_check_in_v(i) {
			if(i=='y'){
				$("#logon_num").val("1");
				$("#logon_num").attr("disabled",true);
			}else{
				
				$("#logon_num").attr("disabled",false);
			}
		}
		
		function android_v(i) {
			if(i==true){
				$("#android_y").css("display", "block");
				$("#android_n").css("display", "none");
			}else{
				$("#android_y").css("display", "none");
				$("#android_n").css("display", "block");
			}
		}
		function ios_v(i) {
			if(i==true){
				$("#ios_y").css("display", "block");
				$("#ios_n").css("display", "none");
			}else{
				$("#ios_y").css("display", "none");
				$("#ios_n").css("display", "block");
			}
		}
		
		function reg_state_v(i) {
			if(i==true){
				$("#reg_state_y").css("display", "block");
				$("#reg_state_n").css("display", "none");
			}else{
				$("#reg_state_y").css("display", "none");
				$("#reg_state_n").css("display", "block");
			}
		}
		
		function logon_state_v(i) {
			if(i==true){
				$("#logon_state_y").css("display", "block");
				$("#logon_state_n").css("display", "none");
			}else{
				$("#logon_state_y").css("display", "none");
				$("#logon_state_n").css("display", "block");
			}
		}
		
		function reg_change() {
			if($('#reg_award').val()=='vip'){
				document.getElementById('reg_award_a').innerHTML="分钟";
			}else{
				document.getElementById('reg_award_a').innerHTML="积分";
			}
		}
		
		function diary_change() {
			if($('#diary_award').val()=='vip'){
				document.getElementById('diary_award_a').innerHTML="分钟";
			}else{
				document.getElementById('diary_award_a').innerHTML="积分";
			}
		}
		
		function inv_change() {
			if($('#inv_award').val()=='vip'){
				document.getElementById('inv_award_a').innerHTML="小时";
			}else{
				document.getElementById('inv_award_a').innerHTML="积分";
			}
		}
		
		$('#copy_id').click(function() {
			let t = window.jQuery;
			var appid="<?php echo $id;?>";
			var oInput = document.createElement('input');
			oInput.value = appid;
			document.body.appendChild(oInput);
			oInput.select(); // 选择对象
			document.execCommand("Copy"); // 执行浏览器复制命令
			oInput.className = 'oInput';
			oInput.style.display='none';
			t.NotificationApp.send("成功",'APPID复制成功',"top-center","rgba(0,0,0,0.2)","success")
		});
		
		$('#copy_key').click(function() {
			let t = window.jQuery;
			var appkey=$("#appkey").val();
			var oInput = document.createElement('input');
			oInput.value = appkey;
			document.body.appendChild(oInput);
			oInput.select(); // 选择对象
			document.execCommand("Copy"); // 执行浏览器复制命令
			oInput.className = 'oInput';
			oInput.style.display='none';
			t.NotificationApp.send("成功",'APPKEY复制成功',"top-center","rgba(0,0,0,0.2)","success")
		});
		
		$('#bian_key').click(function() {
			var appkey=randomString(32);
			$("#appkey").val(appkey);
		});
		
		$('#mi_skey').click(function() {
			var mi_type = $("#mi_type").val();
			if(mi_type == 1){
				var key = randomString(32);
			}else{
				var key = randomString(16);
			}
			$("#mi_rc4_key").val(key);
		});
		
		$('#alipay').click(function() {
			window.open('https://openhome.alipay.com/dev/workspace/key-manage');
			return false;//重要语句：如果是像a链接那种有href属性注册的点击事件，可以阻止它跳转。
		});
		
		function randomString(len) {
			len = len || 32;
			var $chars = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678';    /****默认去掉了容易混淆的字符oOLl,9gq,Vv,Uu,I1****/
			var maxPos = $chars.length;
			var pwd = '';
			for (i = 0; i < len; i++) {
				pwd += $chars.charAt(Math.floor(Math.random() * maxPos));
			}
			return pwd;
		}
		
		function logon_check_in_a(i) {
			if(i=='0'){
				$("#downloadpwd1").val("<?php echo $res['downloadpwd1']; ?>");
				$("#downloadpwd1").attr("disabled",true);
			}else{
				$("#downloadpwd1").attr("disabled",false);
			}
		}
		
		function logon_check_in_b(i) {
			if(i=='0'){
				$("#downloadpwd2").val("<?php echo $res['downloadpwd2']; ?>");
				$("#downloadpwd2").attr("disabled",true);
			}else{
				$("#downloadpwd2").attr("disabled",false);
			}
		}

		$('#mi_skey_aes').click(function() {
			var key = randomString(32);
			$("#mi_aes_key").val(key);
			var key = randomString(16);
			$("#mi_aes_iv").val(key);
		});

		function uploadApk(input) {
			if(!input.files || input.files.length === 0) return;
			var file = input.files[0];
			if(!file.name.toLowerCase().endsWith('.apk')) {
				alert('请选择 APK 文件');
				input.value = '';
				return;
			}
			var progressDiv = document.getElementById('upload_progress');
			var statusDiv = document.getElementById('upload_status');
			var barDiv = document.getElementById('upload_bar');
			progressDiv.style.display = 'block';
			statusDiv.textContent = '上传中: ' + file.name + ' (' + (file.size/1024/1024).toFixed(1) + ' MB)';
			barDiv.style.width = '0%';

			var form = new FormData();
			form.append('apkfile', file);
			form.append('appid', <?php echo isset($res['id']) ? $res['id'] : 0; ?>);
			form.append('appname', 'ysptv');
			var verInput = document.getElementById('android_bb');
			if(verInput && verInput.value) {
				form.append('version', verInput.value);
			}

			var xhr = new XMLHttpRequest();
			xhr.open('POST', '/admin/ajax.php?act=app_upload_apk', true);

			xhr.upload.onprogress = function(e) {
				if(e.lengthComputable) {
					var pct = Math.round((e.loaded / e.total) * 100);
					barDiv.style.width = pct + '%';
					statusDiv.textContent = '上传中: ' + pct + '%';
				}
			};

			xhr.onload = function() {
				if(xhr.status === 200) {
					try {
						var data = JSON.parse(xhr.responseText);
						if(data.code === 200 && data.url) {
							document.getElementById('android_url').value = data.url;
							document.getElementById('downloadtype1').value = '0';
							barDiv.style.width = '100%';
							barDiv.style.background = '#28a745';
							var sizeMB = (data.size / 1024 / 1024).toFixed(1);
							statusDiv.textContent = '✅ 上传成功 (' + sizeMB + ' MB) URL已自动填入';
							setTimeout(function(){ progressDiv.style.display = 'none'; barDiv.style.background = '#007bff'; input.value = ''; }, 4000);
						} else {
							barDiv.style.background = '#dc3545';
							statusDiv.textContent = '❌ ' + (data.msg || '上传失败');
						}
					} catch(err) {
						barDiv.style.background = '#dc3545';
						statusDiv.textContent = '❌ 响应解析失败: ' + xhr.responseText.substring(0,100);
					}
				} else {
					barDiv.style.background = '#dc3545';
					statusDiv.textContent = '❌ HTTP ' + xhr.status;
				}
			};

			xhr.onerror = function() {
				barDiv.style.background = '#dc3545';
				statusDiv.textContent = '❌ 网络错误';
			};

			xhr.send(form);
		}
		
	</script>