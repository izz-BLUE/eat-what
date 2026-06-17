<#
.SYNOPSIS
    今天吃啥 — 管理反馈 CLI 工具

.DESCRIPTION
    通过命令行调用 admin feedback API，方便上线后查看和处理用户反馈。
    MVP 阶段无管理后台 UI，此脚本是唯一的管理工具。

.PARAMETER BaseUrl
    必填，后端 API 地址，例如 https://api.example.com

.PARAMETER Token
    可选，管理后台认证 Token。不传则从环境变量 ADMIN_TOKEN 读取

.PARAMETER Action
    必填，操作类型：list（列表）/ show（详情）/ status（更新状态）

.PARAMETER Status
    可选，按状态筛选：NEW / REVIEWED / RESOLVED / IGNORED（仅 list）

.PARAMETER Keyword
    可选，搜索关键词，匹配 content 和 contact 字段（仅 list）

.PARAMETER Page
    可选，页码，默认 1

.PARAMETER Size
    可选，每页条数，默认 20，最大 100

.PARAMETER Id
    show / status 时必填，反馈 ID

.PARAMETER NewStatus
    status 时必填，新状态：NEW / REVIEWED / RESOLVED / IGNORED

.EXAMPLE
    .\scripts\admin\feedback.ps1 -BaseUrl https://api.example.com -Action list -Status NEW

.EXAMPLE
    .\scripts\admin\feedback.ps1 -BaseUrl https://api.example.com -Action list -Keyword 推荐

.EXAMPLE
    .\scripts\admin\feedback.ps1 -BaseUrl https://api.example.com -Action show -Id 1

.EXAMPLE
    .\scripts\admin\feedback.ps1 -BaseUrl https://api.example.com -Action status -Id 1 -NewStatus REVIEWED
#>

param(
    [Parameter(Mandatory = $true, HelpMessage = "后端 API 地址，例如 https://api.example.com")]
    [string]$BaseUrl,

    [Parameter(HelpMessage = "管理后台认证 Token")]
    [string]$Token,

    [Parameter(Mandatory = $true, HelpMessage = "操作类型")]
    [ValidateSet("list", "show", "status")]
    [string]$Action,

    [Parameter(HelpMessage = "按状态筛选")]
    [ValidateSet("NEW", "REVIEWED", "RESOLVED", "IGNORED")]
    [string]$Status,

    [Parameter(HelpMessage = "搜索关键词，匹配 content/contact")]
    [string]$Keyword,

    [Parameter(HelpMessage = "页码，默认 1")]
    [int]$Page = 1,

    [Parameter(HelpMessage = "每页条数，默认 20")]
    [int]$Size = 20,

    [Parameter(HelpMessage = "反馈 ID（show/status 时必填）")]
    [int]$Id,

    [Parameter(HelpMessage = "新状态（status 时必填）")]
    [ValidateSet("NEW", "REVIEWED", "RESOLVED", "IGNORED")]
    [string]$NewStatus
)

$ErrorActionPreference = "Stop"

# ============================================================
# 参数预处理
# ============================================================

# 去掉 BaseUrl 末尾斜杠
$BaseUrl = $BaseUrl -replace '/+$', ''

# 解析 Token：参数 > 环境变量
if (-not $Token) {
    $Token = $env:ADMIN_TOKEN
}
if (-not $Token) {
    Write-Host "[ERROR] 缺少认证 Token" -ForegroundColor Red
    Write-Host '  请通过 -Token 参数传入，或设置环境变量: $env:ADMIN_TOKEN="xxx"'
    exit 1
}

# 验证 Action 特定参数
if ($Action -eq "show" -or $Action -eq "status") {
    if (-not $PSBoundParameters.ContainsKey('Id')) {
        Write-Host "[ERROR] $Action 操作需要 -Id 参数" -ForegroundColor Red
        Write-Host "  示例: .\scripts\admin\feedback.ps1 -BaseUrl https://api.example.com -Action $Action -Id 1"
        exit 1
    }
}

if ($Action -eq "status") {
    if (-not $PSBoundParameters.ContainsKey('NewStatus')) {
        Write-Host "[ERROR] status 操作需要 -NewStatus 参数" -ForegroundColor Red
        Write-Host "  示例: .\scripts\admin\feedback.ps1 -BaseUrl https://api.example.com -Action status -Id 1 -NewStatus REVIEWED"
        exit 1
    }
}

# 限制 Size 最大值与 API 一致
if ($Size -gt 100) {
    $Size = 100
}
if ($Page -lt 1) {
    $Page = 1
}

# ============================================================
# 工具函数
# ============================================================

function Invoke-Api {
    <#
    .SYNOPSIS
        调用后端 API，自动附加 X-Admin-Token 并检查响应 code
    #>
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body
    )

    $headers = @{
        "X-Admin-Token" = $Token
        "Content-Type"  = "application/json"
    }
    $uri = "$BaseUrl$Path"

    try {
        if ($Body) {
            $jsonBody = $Body | ConvertTo-Json -Compress
            $response = Invoke-RestMethod -Uri $uri -Method $Method -Headers $headers -Body $jsonBody
        }
        else {
            $response = Invoke-RestMethod -Uri $uri -Method $Method -Headers $headers
        }

        if ($response.code -ne 0) {
            Write-Host "[ERROR] API 返回错误: code=$($response.code), message=$($response.message)" -ForegroundColor Red
            exit 1
        }

        return $response.data
    }
    catch [System.Net.WebException] {
        $msg = $_.Exception.Message
        Write-Host "[ERROR] 网络错误: $msg" -ForegroundColor Red
        if ($_.Exception.Response) {
            try {
                $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
                $body = $reader.ReadToEnd()
                if ($body) {
                    Write-Host "  响应内容: $body" -ForegroundColor DarkGray
                }
            }
            catch { }
        }
        exit 1
    }
    catch {
        Write-Host "[ERROR] 请求失败: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
}

function Format-Content {
    <#
    .SYNOPSIS
        截断内容到指定长度，超出加 "..."
    #>
    param([string]$Text, [int]$MaxLen = 40)

    if (-not $Text) { return "" }
    $trimmed = $Text.Trim()
    if ($trimmed.Length -le $MaxLen) {
        return $trimmed
    }
    return $trimmed.Substring(0, $MaxLen) + "..."
}

function Format-Date {
    <#
    .SYNOPSIS
        格式化日期时间
    #>
    param($Date)

    if (-not $Date) { return "N/A" }
    try {
        $dt = [DateTime]$Date
        return $dt.ToString("yyyy-MM-dd HH:mm:ss")
    }
    catch {
        return $Date.ToString()
    }
}

function Format-Empty {
    <#
    .SYNOPSIS
        空值 / 空字符串显示为 "-"
    #>
    param($Value)

    if ($null -eq $Value -or $Value.ToString().Trim().Length -eq 0) {
        return "-"
    }
    return $Value.ToString()
}

# ============================================================
# 各 Action 实现
# ============================================================

function Invoke-ListAction {
    <#
    .SYNOPSIS
        list — 查询反馈列表，表格输出
    #>
    $queryParams = @()
    if ($Status)  { $queryParams += "status=$Status" }
    if ($Keyword) { $queryParams += "keyword=$([uri]::EscapeDataString($Keyword))" }
    $queryParams += "page=$Page"
    $queryParams += "size=$Size"

    $sep = [char]38  # '&' character
    $path = "/api/v1/admin/feedback?" + ($queryParams -join $sep)
    $data = Invoke-Api -Method "GET" -Path $path

    if (-not $data -or -not $data.items -or $data.items.Count -eq 0) {
        Write-Host "(无结果)"
        if ($data) {
            $totalPages = [Math]::Ceiling($data.total / $data.size)
            Write-Host "  共 $($data.total) 条，第 $($data.page)/$totalPages 页"
        }
        return
    }

    # 表格输出
    $rows = @()
    foreach ($item in $data.items) {
        $rows += [PSCustomObject]@{
            id        = $item.id
            type      = Format-Empty $item.type
            rating    = if ($item.rating) { "$($item.rating)/5" } else { "-" }
            status    = Format-Empty $item.status
            createdAt = Format-Date $item.createdAt
            content   = Format-Content $item.content 40
            contact   = Format-Empty $item.contact
        }
    }

    Write-Host ""
    $rows | Format-Table -AutoSize
    $totalPages = [Math]::Ceiling($data.total / $data.size)
    Write-Host "共 $($data.total) 条，第 $($data.page)/$totalPages 页"
}

function Invoke-ShowAction {
    <#
    .SYNOPSIS
        show — 查看单条反馈详情，遍历 list 按 id 查找
    #>
    $searchSize = 100
    $currentPage = 1
    $found = $null

    while ($true) {
        $sep = [char]38  # '&' character
        $path = "/api/v1/admin/feedback?page=$currentPage" + $sep + "size=$searchSize"
        $data = Invoke-Api -Method "GET" -Path $path

        if (-not $data -or -not $data.items -or $data.items.Count -eq 0) {
            break
        }

        foreach ($item in $data.items) {
            if ($item.id -eq $Id) {
                $found = $item
                break
            }
        }

        if ($found) { break }

        # 检查是否还有下一页
        $totalPages = [Math]::Ceiling($data.total / $searchSize)
        if ($currentPage -ge $totalPages) {
            break
        }
        $currentPage++
    }

    if (-not $found) {
        Write-Host "[WARN] 未找到反馈 id=$Id" -ForegroundColor Yellow
        exit 1
    }

    # 输出完整字段
    Write-Host ""
    Write-Host "========================================"
    Write-Host "  反馈详情 #$Id"
    Write-Host "========================================"
    Write-Host ""
    Write-Host ("{0,-14}: {1}" -f "id",         $(Format-Empty $found.id))
    Write-Host ("{0,-14}: {1}" -f "type",       $(Format-Empty $found.type))
    Write-Host ("{0,-14}: {1}" -f "rating",     $(if ($found.rating) { "$($found.rating)/5" } else { "-" }))
    Write-Host ("{0,-14}: {1}" -f "status",     $(Format-Empty $found.status))
    Write-Host ("{0,-14}: {1}" -f "content",    $(Format-Empty $found.content))
    Write-Host ("{0,-14}: {1}" -f "contact",    $(Format-Empty $found.contact))
    Write-Host ("{0,-14}: {1}" -f "page",       $(Format-Empty $found.page))
    Write-Host ("{0,-14}: {1}" -f "systemInfo", $(Format-Empty $found.systemInfo))
    Write-Host ("{0,-14}: {1}" -f "createdAt",  $(Format-Date $found.createdAt))
    Write-Host ""
    Write-Host "注意: userId / updatedAt 字段当前 API 版本未返回" -ForegroundColor DarkGray
}

function Invoke-StatusAction {
    <#
    .SYNOPSIS
        status — 更新反馈状态
    #>
    $body = @{ status = $NewStatus }
    $path = "/api/v1/admin/feedback/$Id/status"
    $data = Invoke-Api -Method "PUT" -Path $path -Body $body

    Write-Host ""
    Write-Host "========================================"
    Write-Host "  状态更新成功"
    Write-Host "========================================"
    Write-Host ""
    Write-Host ("{0,-14}: {1}" -f "id",        $(Format-Empty $data.id))
    Write-Host ("{0,-14}: {1}" -f "status",    $(Format-Empty $data.status))
    Write-Host ("{0,-14}: {1}" -f "createdAt", $(Format-Date $data.createdAt))
    Write-Host ""
    Write-Host "注意: updatedAt 字段当前 API 版本未返回" -ForegroundColor DarkGray
}

# ============================================================
# 入口
# ============================================================

switch ($Action) {
    "list"   { Invoke-ListAction }
    "show"   { Invoke-ShowAction }
    "status" { Invoke-StatusAction }
}
