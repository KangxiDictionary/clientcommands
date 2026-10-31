# 功能自检清单

安装后按顺序做一遍，可以把"模组坏了"和"某个功能本来就这样"区分开。
任何一步失败，先跑 `/cdiag`，把输出和 `logs/latest.log` 里含 `[clientcommands]` 的行一起贴出来。

## 0. 一分钟冒烟测试

| # | 操作 | 期望 |
| --- | --- | --- |
| 1 | 进入单人世界 | 正常进世界，无崩溃 |
| 2 | `/cdiag` | 输出一段诊断信息，见下 |
| 3 | 输入 `/` 看列表 | 出现 `c` 开头的命令 |
| 4 | `/cconfig` | 显示第 1 页设置，条目可点击 |
| 5 | `/cdebug` | 切换 F3 调试界面 |

`/cdiag` 正常输出示例：

```
clientcommands 诊断
模组 2.16.3-neoforge+mc26.3 | Minecraft 26.3 | NeoForge 26.3.0.99
已注册命令 69 条 | 执行源 OK | 补全源 OK
访问转换器：OK
近战 RNG 钩子：disabled      ← 已知限制，见文末
配置：22 个选项，位于 .../config/clientcommands/config.json
随机数：玩家种子 uncracked | 附魔 uncracked | 维护 OK
世界 minecraft:overworld @ (0, 64, 0)，实体 120 个
```

判定：`执行源`/`补全源`/`访问转换器` 任一为 `MISSING` 都说明平台层没装好，直接报 bug。

## 1. 命令注册与补全

| 操作 | 期望 |
| --- | --- |
| `/c` + Tab | 列出所有 `c` 开头命令 |
| `/cconfig ` + Tab | 补出 `list` / `get` / `set` |
| `/cconfig set enchantingPrediction ` + Tab | 补出 `true` / `false` |
| `/cconfig set fishingManipulation ` + Tab | 补出 `off` / `manual` / ... |
| `/cconfig set ` 输入错误选项 | 报"未知设置" |

## 2. 实体选择器

| 操作 | 期望 |
| --- | --- |
| `/cglow entities @e` | 附近实体发光 |
| `/cglow entities @e[name=xxx]` | 只有匹配名字的实体发光 |
| 输入 `@e[` + Tab | 补出 `name=`、`type=`、`distance=` 等选项 |
| 输入 `@e[type=` + Tab | 补出实体类型 |

## 3. 发光渲染（cglow）

| 操作 | 期望 |
| --- | --- |
| `/cglow entities @e 10` | 10 秒后同时熄灭 |
| `/cglow entities @e 10 color red` | 红色轮廓 |
| `/cglow block ~ ~ ~ 10` | 方块轮廓 |
| `/cglow area ~ ~ ~ ~5 ~5 ~5 10` | 区域线框 |
| `/cglow entities @e --keep-searching` | 持续为新出现的实体发光，可用 `/ctask stop` 结束 |
| 日志搜 `render submission reached` | 出现一次，说明渲染注入点生效 |

## 4. 配置（cconfig）

| 操作 | 期望 |
| --- | --- |
| `/cconfig` | 分页列表，`上一页/下一页` 可点 |
| 点设置名 | 聊天栏显示解释 + 值 + 类型 |
| 点设置值 | 输入框被填入 `/cconfig set <选项> <建议值>`，**不会立即修改** |
| `/cconfig get maxEnchantItemThrows` | 显示解释、值和类型 |
| `/cconfig set overriddenFps 120` | 显示 `overriddenFps = 120`，重启后仍保留 |
| 手动编辑 `config/clientcommands/config.json` 后重启 | 值生效 |

## 5. RNG 破解与附魔预测

前置：`/cconfig set enchantingPrediction true`、`/cconfig set playerRNGMaintenance true`（默认开）。

| 操作 | 期望 |
| --- | --- |
| 连续丢物品若干次 | 聊天栏出现 RNG 进度提示 |
| `/ccrackrng` | 开始破解玩家种子，进度可查 |
| 破解完成后 `/cenchant diamond_pickaxe fortune=3` | 输出需要丢物品次数、书架数、槽位、附魔结果 |
| 加 `--simulate` | 只计算不实际附魔 |
| `/cenchant` 时种子未破解 | 提示 `uncracked`，并给出 `/ccrackrng` 可点击提示 |

## 6. 其它常用命令

| 命令 | 期望 |
| --- | --- |
| `/cfind`、`/cfindblock`、`/cfinditem` | 找到目标后输出坐标并可点击传送/发光 |
| `/careastats` | 统计区域方块 |
| `/cwiki <主题>` | 打开对应 wiki 页面 |
| `/calias` | 别名增删查 |
| `/ctask` | 查看/停止后台任务 |
| `/crelog` | 断线界面出现倒计时重连按钮 |

## 7. 稳定性

| 检查 | 期望 |
| --- | --- |
| 连续玩 10 分钟并切换维度 | 无崩溃、无 `ClassCastException` |
| 断开重连服务器 | 命令仍在，`/cdiag` 仍正常 |
| 日志搜 `Exception` / `Failed to register` | 与本模组相关的应为空 |

## 故障对照表

| 现象 | 可能原因 | 处理 |
| --- | --- | --- |
| `/` 里没有 `c` 命令 | 注册失败 | 看日志 `failed to register command X`，把 X 报上来 |
| 补全整个消失 | 补全路径抛异常 | 看日志 `Exception`，报上来 |
| 提示"未知设置" | 选项名拼错 | `/cconfig` 列表里复制 |
| 附魔预测结果偏差 | 攻击过怪物导致 RNG 偏移（已知限制） | 重新 `/ccrackrng`，避免近战 |
| 发光时间不一致 | 分批执行或实体停止 tick | 同一条命令、`seconds` 参数一致时应当同步 |
| `cdiag` 显示 `MISSING` | 平台层未生效 | 确认用的是 NeoForge 版 jar 且未被其他模组覆盖 |

## 已知限制

- 近战攻击的 RNG 消耗未追踪（`rngevents.PlayerMixin` 因 MixinExtras `VerifyError` 暂时禁用）：
  攻击后玩家 RNG 会偏移，`/ccrackrng` 需要重新跑。
- 没有配置 GUI，只有 `/cconfig` 命令界面。
