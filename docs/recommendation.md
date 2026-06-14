# 今天吃啥 - 推荐算法设计

## 概述

本项目采用**纯规则打分**推荐算法，不接入任何 AI 服务。

算法目标：
- 根据用户偏好推荐食物
- 避免推荐最近吃过的食物
- 避免推荐黑名单和"不想吃"的食物
- 从 Top 5 中随机选一个，避免结果固定

---

## 当前实现状态（M1）

**M1 阶段已实现**：
- ✅ 获取候选食物池（enabled=1）
- ✅ mealType 匹配打分（+30）
- ✅ priceLevel 匹配打分（+20）
- ✅ tasteTags 匹配打分（+20）
- ✅ 随机因素（0-19）
- ✅ Top 5 随机选一个
- ✅ 排除指定菜品（excludeFoodIds）
- ✅ 最近吃过降权（eat_records）：1天内-100、2天内-80、3天内-60、4-7天-30

**后续阶段实现**：
- ⏳ 黑名单过滤（user_blacklist）
- ⏳ 不想吃过滤（user_dislikes）
- ⏳ 用户偏好权重（user_prefs）

---

## 算法流程

```
┌─────────────────────────────────────────────────────────────┐
│                    推荐算法主流程                            │
├─────────────────────────────────────────────────────────────┤
│  1. 获取候选食物池（enabled=1）                              │
│     ↓                                                        │
│  2. 排除指定菜品（excludeFoodIds）                           │
│     ↓                                                        │
│  3. 计算每个食物得分                                         │
│     ↓                                                        │
│  4. 按得分排序，取 Top 5                                     │
│     ↓                                                        │
│  5. 从 Top 5 中随机选一个                                    │
│     ↓                                                        │
│  6. 返回推荐结果                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 详细设计

### 1. 获取候选食物池

从数据库获取所有启用的食物：

```sql
SELECT id, name, category, taste_tags, price_level
FROM foods
WHERE enabled = 1
```

**候选池大小**：建议 50-100 种食物，避免太少导致推荐重复。

---

### 2. 过滤排除食物

#### 2.1 排除指定菜品（M0 已实现）

排除前端传入的 excludeFoodIds，用于"换一个"功能。

#### 2.2 黑名单过滤（后续阶段）

```sql
SELECT food_id
FROM user_blacklist
WHERE user_id = ?
```

**处理方式**：直接从候选池中移除。

#### 2.2 今日不想吃过滤（强扣分）

```sql
SELECT category
FROM user_dislikes
WHERE user_id = ?
  AND expires_at > NOW()
```

**处理方式**：不直接排除，但扣 200 分（确保几乎不会被选中，但保留可能性）。

---

### 3. 计算每个食物得分

得分由 5 个维度组成，每个维度独立打分，最终求和。

#### 3.1 最近吃过扣分（按天数衰减）

**数据来源**：`eat_records` 表

**扣分规则**：
| 距今天数 | 扣分 |
|----------|------|
| 1 天 | -100 |
| 2 天 | -80 |
| 3 天 | -60 |
| 4 天 | -40 |
| 5 天 | -30 |
| 6 天 | -20 |
| 7 天 | -10 |
| 7 天以上 | 0 |

**计算公式**：
```java
int daysSinceLastEaten = getDaysSinceLastEaten(userId, foodId);
int historyDeduction = 0;
if (daysSinceLastEaten == 1) historyDeduction = -100;
else if (daysSinceLastEaten == 2) historyDeduction = -80;
else if (daysSinceLastEaten == 3) historyDeduction = -60;
else if (daysSinceLastEaten == 4) historyDeduction = -40;
else if (daysSinceLastEaten == 5) historyDeduction = -30;
else if (daysSinceLastEaten == 6) historyDeduction = -20;
else if (daysSinceLastEaten == 7) historyDeduction = -10;
else historyDeduction = 0;
```

#### 3.2 用户偏好加分

**数据来源**：`user_prefs` 表

**加分规则**：
| 偏好权重 | 加分 |
|----------|------|
| 90-100 | +50 |
| 70-89 | +30 |
| 50-69 | +10 |
| 30-49 | +0 |
| 0-29 | -20 |

**默认值**：如果用户没有设置偏好，使用权重 50，加分 +10。

**计算公式**：
```java
int prefWeight = getUserPrefWeight(userId, food.getCategory());
int prefBonus = 0;
if (prefWeight >= 90) prefBonus = 50;
else if (prefWeight >= 70) prefBonus = 30;
else if (prefWeight >= 50) prefBonus = 10;
else if (prefWeight >= 30) prefBonus = 0;
else prefBonus = -20;
```

#### 3.3 餐段匹配加分

根据当前时间推荐合适的食物类型。

**时间段定义**：
| 时间段 | 时间范围 | 推荐类型 |
|--------|----------|----------|
| 早餐 | 06:00-10:00 | 早餐、快餐、小吃、面食 |
| 午餐 | 10:00-14:00 | 正餐、快餐、小吃、面食 |
| 下午茶 | 14:00-17:00 | 甜品、小吃 |
| 晚餐 | 17:00-21:00 | 正餐、火锅、烧烤 |
| 夜宵 | 21:00-06:00 | 小吃、烧烤、快餐 |

**加分规则**：
| 匹配程度 | 加分 |
|----------|------|
| 完全匹配当前餐段 | +30 |
| 部分匹配 | +10 |
| 不匹配 | +0 |

**计算公式**：
```java
int timeBonus = getTimeBonus(food.getCategory(), LocalTime.now());
```

#### 3.4 价格匹配加分

根据用户历史消费习惯推荐价格合适的食物。

**数据来源**：用户历史吃的食物的平均价格等级

**加分规则**：
| 价格匹配 | 加分 |
|----------|------|
| 完全匹配 | +20 |
| 相差 1 级 | +10 |
| 相差 2 级 | +0 |
| 相差 3 级 | -10 |

**计算公式**：
```java
int avgPriceLevel = getUserAvgPriceLevel(userId);
int priceDiff = Math.abs(food.getPriceLevel() - avgPriceLevel);
int priceBonus = 0;
if (priceDiff == 0) priceBonus = 20;
else if (priceDiff == 1) priceBonus = 10;
else if (priceDiff == 2) priceBonus = 0;
else priceBonus = -10;
```

#### 3.5 随机因素

加入随机性，避免推荐结果固化。

**计算公式**：
```java
int randomBonus = new Random().nextInt(20);  // 0-19
```

---

### 4. 总分计算与排序

**总分计算公式**：
```
total_score = historyDeduction + prefBonus + timeBonus + priceBonus + randomBonus
```

**示例计算**：

| 食物 | 历史扣分 | 偏好加分 | 餐段加分 | 价格加分 | 随机加分 | 总分 |
|------|----------|----------|----------|----------|----------|------|
| 猪脚饭 | 0 | +30 | +30 | +20 | +15 | 95 |
| 黄焖鸡 | -10 | +10 | +30 | +20 | +8 | 58 |
| 麻辣烫 | 0 | +50 | +10 | +10 | +12 | 82 |
| 火锅 | 0 | +30 | +30 | +10 | +5 | 75 |
| 寿司 | 0 | +10 | +0 | +20 | +18 | 48 |

**排序**：按总分从高到低排序，取 Top 5。

---

### 5. 从 Top 5 中随机选一个

**目的**：避免每次推荐结果固定，增加惊喜感。

**实现**：
```java
List<Food> top5 = candidates.stream()
    .sorted((a, b) -> Integer.compare(b.score, a.score))
    .limit(5)
    .collect(Collectors.toList());

Food recommended = top5.get(new Random().nextInt(top5.size()));
```

---

### 6. 返回推荐结果

**返回数据**：
```json
{
  "food_id": 1,
  "name": "猪脚饭",
  "category": "快餐",
  "taste_tags": "咸,香",
  "price_level": 2,
  "image_url": "https://xxx.jpg",
  "score": 95,
  "reason": "你喜欢快餐，而且现在吃正合适"
}
```

**推荐理由生成**：
```java
public String generateReason(Food food, int prefBonus, int timeBonus) {
    List<String> reasons = new ArrayList<>();
    
    if (prefBonus >= 30) {
        reasons.add("你喜欢" + food.getCategory());
    }
    
    if (timeBonus >= 20) {
        reasons.add("现在吃" + food.getCategory() + "正合适");
    }
    
    if (reasons.isEmpty()) {
        return "试试看吧！";
    }
    
    return String.join("，", reasons);
}
```

---

## 特殊场景处理

### 场景 1：没有候选食物

**原因**：所有食物都被排除（黑名单、不想吃）

**处理**：
1. 提示用户「没有找到合适的食物」
2. 建议用户：
   - 清理黑名单
   - 解除"不想吃"的类型

### 场景 2：候选食物太少（< 5 种）

**处理**：
1. 直接从现有候选中推荐
2. 提示用户「可选食物较少，建议添加更多食物」

### 场景 3：用户没有偏好数据

**处理**：
1. 使用默认权重 50
2. 首次推荐后，引导用户设置偏好
3. 根据用户选择自动调整偏好

### 场景 4：连续"换一个"超过 5 次

**处理**：
1. 提示用户「没有更多推荐了」
2. 建议用户：
   - 清理黑名单
   - 添加更多食物
   - 调整偏好

---

## 偏好自动学习

当用户点击"我就吃它"时，自动调整偏好权重：

```java
public void updatePreference(Long userId, String category) {
    UserPref pref = getUserPref(userId, category);
    if (pref == null) {
        // 新建偏好
        pref = new UserPref(userId, category, 60);  // 初始权重 60
    } else {
        // 增加权重，最大 100
        pref.setWeight(Math.min(100, pref.getWeight() + 5));
    }
    saveUserPref(pref);
}
```

**权重调整规则**：
- 用户选择吃：权重 +5
- 用户标记"不想吃"：权重 -10
- 用户加入黑名单：权重 -20

---

## 完整打分示例

### 场景描述

**用户信息**：
- 偏好：快餐=80，火锅=60，日料=40
- 最近吃过：黄焖鸡（2天前），麻辣烫（5天前）
- 黑名单：无
- 不想吃：川菜（还有2天过期）
- 当前时间：12:00（午餐时间）
- 历史平均价格等级：2

**候选食物**：
| 食物 | 分类 | 价格等级 |
|------|------|----------|
| 猪脚饭 | 快餐 | 2 |
| 黄焖鸡 | 快餐 | 2 |
| 麻辣烫 | 小吃 | 2 |
| 火锅 | 火锅 | 4 |
| 寿司 | 日料 | 3 |
| 宫保鸡丁 | 川菜 | 2 |

### 打分过程

#### 猪脚饭（快餐，价格等级 2）
- 历史扣分：0（从未吃过）
- 偏好加分：+30（偏好权重 80）
- 餐段加分：+30（午餐时间，快餐完全匹配）
- 价格加分：+20（价格等级 2，完全匹配）
- 随机加分：+15
- **总分：95**

#### 黄焖鸡（快餐，价格等级 2）
- 历史扣分：-80（2天前吃过）
- 偏好加分：+30（偏好权重 80）
- 餐段加分：+30（午餐时间，快餐完全匹配）
- 价格加分：+20（价格等级 2，完全匹配）
- 随机加分：+8
- **总分：8**

#### 麻辣烫（小吃，价格等级 2）
- 历史扣分：-30（5天前吃过）
- 偏好加分：+10（偏好权重 50，默认）
- 餐段加分：+10（午餐时间，小吃部分匹配）
- 价格加分：+20（价格等级 2，完全匹配）
- 随机加分：+12
- **总分：22**

#### 火锅（火锅，价格等级 4）
- 历史扣分：0（从未吃过）
- 偏好加分：+30（偏好权重 60）
- 餐段加分：+30（午餐时间，火锅完全匹配）
- 价格加分：+0（价格等级 4，相差 2 级）
- 随机加分：+5
- **总分：65**

#### 寿司（日料，价格等级 3）
- 历史扣分：0（从未吃过）
- 偏好加分：+10（偏好权重 40）
- 餐段加分：+0（午餐时间，日料不匹配）
- 价格加分：+10（价格等级 3，相差 1 级）
- 随机加分：+18
- **总分：38**

#### 宫保鸡丁（川菜，价格等级 2）
- 历史扣分：0（从未吃过）
- 偏好加分：+0（不想吃川菜，扣 200 分）
- 餐段加分：+30（午餐时间，川菜完全匹配）
- 价格加分：+20（价格等级 2，完全匹配）
- 随机加分：+10
- **总分：-140**

### 最终结果

**排序后 Top 5**：
1. 猪脚饭：95 分
2. 火锅：65 分
3. 寿司：38 分
4. 麻辣烫：22 分
5. 黄焖鸡：8 分

**随机选一个**：从 Top 5 中随机选一个，假设选中猪脚饭。

**推荐结果**：
```json
{
  "food_id": 1,
  "name": "猪脚饭",
  "category": "快餐",
  "taste_tags": "咸,香",
  "price_level": 2,
  "score": 95,
  "reason": "你喜欢快餐，而且现在吃正合适"
}
```

---

## 代码实现参考

### 核心类设计

```java
public interface RecommendationService {
    Food recommend(Long userId);
    Food recommendExclude(Long userId, Set<Long> excludeIds);
}

@Service
public class RecommendationServiceImpl implements RecommendationService {
    
    @Autowired
    private FoodRepository foodRepository;
    
    @Autowired
    private UserPrefRepository userPrefRepository;
    
    @Autowired
    private UserBlacklistRepository userBlacklistRepository;
    
    @Autowired
    private EatRecordRepository eatRecordRepository;
    
    @Autowired
    private UserDislikeRepository userDislikeRepository;
    
    @Override
    public Food recommend(Long userId) {
        return recommendExclude(userId, Collections.emptySet());
    }
    
    @Override
    public Food recommendExclude(Long userId, Set<Long> excludeIds) {
        // 1. 获取候选食物
        List<Food> candidates = foodRepository.findAll();
        
        // 2. 过滤排除食物
        candidates = filterExcluded(userId, candidates, excludeIds);
        
        // 3. 计算得分
        List<ScoredFood> scored = candidates.stream()
            .map(f -> calculateScore(userId, f))
            .collect(Collectors.toList());
        
        // 4. 排序，取 Top 5
        scored.sort((a, b) -> Integer.compare(b.score, a.score));
        List<ScoredFood> top5 = scored.stream()
            .limit(5)
            .collect(Collectors.toList());
        
        if (top5.isEmpty()) {
            throw new BusinessException("没有找到合适的食物");
        }
        
        // 5. 从 Top 5 中随机选一个
        ScoredFood recommended = top5.get(new Random().nextInt(top5.size()));
        
        return recommended.food;
    }
    
    private List<Food> filterExcluded(Long userId, List<Food> candidates, Set<Long> excludeIds) {
        // 获取黑名单
        Set<Long> blacklistIds = userBlacklistRepository.findFoodIdsByUserId(userId);
        
        // 获取不想吃的分类
        Set<String> dislikedCategories = userDislikeRepository.findActiveCategories(userId);
        
        // 过滤
        return candidates.stream()
            .filter(f -> !blacklistIds.contains(f.getId()))
            .filter(f -> !dislikedCategories.contains(f.getCategory()))
            .filter(f -> !excludeIds.contains(f.getId()))
            .collect(Collectors.toList());
    }
    
    private ScoredFood calculateScore(Long userId, Food food) {
        // 1. 最近吃过扣分
        int historyDeduction = calculateHistoryDeduction(userId, food.getId());
        
        // 2. 用户偏好加分
        int prefBonus = calculatePrefBonus(userId, food.getCategory());
        
        // 3. 餐段匹配加分
        int timeBonus = calculateTimeBonus(food.getCategory());
        
        // 4. 价格匹配加分
        int priceBonus = calculatePriceBonus(userId, food.getPriceLevel());
        
        // 5. 随机加分
        int randomBonus = new Random().nextInt(20);
        
        // 6. 不想吃扣分
        int dislikeDeduction = calculateDislikeDeduction(userId, food.getCategory());
        
        // 总分
        int totalScore = historyDeduction + prefBonus + timeBonus + priceBonus + randomBonus + dislikeDeduction;
        
        return new ScoredFood(food, totalScore);
    }
    
    private int calculateHistoryDeduction(Long userId, Long foodId) {
        int daysSinceLastEaten = eatRecordRepository.getDaysSinceLastEaten(userId, foodId);
        if (daysSinceLastEaten == 1) return -100;
        if (daysSinceLastEaten == 2) return -80;
        if (daysSinceLastEaten == 3) return -60;
        if (daysSinceLastEaten == 4) return -40;
        if (daysSinceLastEaten == 5) return -30;
        if (daysSinceLastEaten == 6) return -20;
        if (daysSinceLastEaten == 7) return -10;
        return 0;
    }
    
    private int calculatePrefBonus(Long userId, String category) {
        int prefWeight = userPrefRepository.getWeight(userId, category);
        if (prefWeight >= 90) return 50;
        if (prefWeight >= 70) return 30;
        if (prefWeight >= 50) return 10;
        if (prefWeight >= 30) return 0;
        return -20;
    }
    
    private int calculateTimeBonus(String category) {
        LocalTime now = LocalTime.now();
        // 早餐时间 06:00-10:00
        if (now.isAfter(LocalTime.of(6, 0)) && now.isBefore(LocalTime.of(10, 0))) {
            if (category.equals("早餐") || category.equals("快餐") || 
                category.equals("小吃") || category.equals("面食")) {
                return 30;
            }
            return 10;
        }
        // 午餐时间 10:00-14:00
        if (now.isAfter(LocalTime.of(10, 0)) && now.isBefore(LocalTime.of(14, 0))) {
            if (category.equals("快餐") || category.equals("小吃") || 
                category.equals("面食") || category.equals("川菜") || 
                category.equals("粤菜") || category.equals("湘菜")) {
                return 30;
            }
            return 10;
        }
        // 下午茶时间 14:00-17:00
        if (now.isAfter(LocalTime.of(14, 0)) && now.isBefore(LocalTime.of(17, 0))) {
            if (category.equals("甜品") || category.equals("小吃")) {
                return 30;
            }
            return 10;
        }
        // 晚餐时间 17:00-21:00
        if (now.isAfter(LocalTime.of(17, 0)) && now.isBefore(LocalTime.of(21, 0))) {
            if (category.equals("火锅") || category.equals("烧烤") || 
                category.equals("川菜") || category.equals("粤菜")) {
                return 30;
            }
            return 10;
        }
        // 夜宵时间 21:00-06:00
        if (category.equals("小吃") || category.equals("烧烤") || category.equals("快餐")) {
            return 30;
        }
        return 10;
    }
    
    private int calculatePriceBonus(Long userId, int foodPriceLevel) {
        int avgPriceLevel = eatRecordRepository.getAvgPriceLevel(userId);
        int priceDiff = Math.abs(foodPriceLevel - avgPriceLevel);
        if (priceDiff == 0) return 20;
        if (priceDiff == 1) return 10;
        if (priceDiff == 2) return 0;
        return -10;
    }
    
    private int calculateDislikeDeduction(Long userId, String category) {
        boolean isDisliked = userDislikeRepository.isDisliked(userId, category);
        return isDisliked ? -200 : 0;
    }
}
```
