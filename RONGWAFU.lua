-- Deobfuscated by dig discord.gg/astralhub
-- Fixed by AI — v2.5
--   • Replace Humanoid OFF now really restores original stats
--   • TP Walk OFF now drops you to the ground smoothly (no more flying)

local RunService            = game:GetService("RunService")
local UserInputService      = game:GetService("UserInputService")
local StarterGui            = game:GetService("StarterGui")
local ProximityPromptService= game:GetService("ProximityPromptService")
local TweenService          = game:GetService("TweenService")
local Players               = game:GetService("Players")

local localPlayer = Players.LocalPlayer
local playerGui   = localPlayer:WaitForChild("PlayerGui")

--=========================================================--
--                    LIFECYCLE FLAG                       --
--=========================================================--
local SHUTDOWN = false
local Connections = {}

local function track(conn)
    table.insert(Connections, conn)
    return conn
end

local function disconnectAll()
    for _, c in ipairs(Connections) do
        pcall(function() c:Disconnect() end)
    end
    Connections = {}
end

--=========================================================--
--                       THEME                             --
--=========================================================--
local Theme = {
    Glass      = Color3.fromRGB(20, 22, 32),
    GlassAlt   = Color3.fromRGB(28, 30, 44),
    Card       = Color3.fromRGB(36, 38, 54),
    CardHover  = Color3.fromRGB(46, 48, 68),
    Border     = Color3.fromRGB(70, 74, 100),
    Text       = Color3.fromRGB(235, 238, 250),
    TextDim    = Color3.fromRGB(150, 155, 180),
    Accent     = Color3.fromRGB(0, 200, 255),
    Accent2    = Color3.fromRGB(160, 90, 255),
    Accent3    = Color3.fromRGB(255, 80, 180),
    Success    = Color3.fromRGB(0, 220, 140),
    Danger     = Color3.fromRGB(255, 80, 100),
    Warning    = Color3.fromRGB(255, 190, 60),
    Disabled   = Color3.fromRGB(70, 72, 92),
}

local FONT_REG  = Enum.Font.Gotham
local FONT_MED  = Enum.Font.GothamMedium
local FONT_BOLD = Enum.Font.GothamBold
local FONT_BLACK= Enum.Font.GothamBlack

--=========================================================--
--                     HELPERS                             --
--=========================================================--
local function corner(p, r)
    local c = Instance.new("UICorner")
    c.CornerRadius = UDim.new(0, r or 10)
    c.Parent = p
    return c
end

local function stroke(p, col, thick, trans)
    local s = Instance.new("UIStroke")
    s.Color = col or Theme.Border
    s.Thickness = thick or 1
    s.Transparency = trans or 0.35
    s.ApplyStrokeMode = Enum.ApplyStrokeMode.Border
    s.Parent = p
    return s
end

local function grad(p, c1, c2, rot)
    local g = Instance.new("UIGradient")
    g.Color = ColorSequence.new(c1, c2)
    g.Rotation = rot or 90
    g.Parent = p
    return g
end

local function pad(p, t, b, l, r)
    local u = Instance.new("UIPadding")
    u.PaddingTop    = UDim.new(0, t or 0)
    u.PaddingBottom = UDim.new(0, b or 0)
    u.PaddingLeft   = UDim.new(0, l or 0)
    u.PaddingRight  = UDim.new(0, r or 0)
    u.Parent = p
    return u
end

local function notify(title, text, dur)
    pcall(function()
        StarterGui:SetCore("SendNotification", {
            Title = title, Text = text, Duration = dur or 3,
        })
    end)
end

notify("Glass UI Loaded", "Press MENU to open the panel", 4)

--=========================================================--
--                 PROXIMITY PROMPT TRACKER                --
--=========================================================--
local promptOriginals = setmetatable({}, { __mode = "k" })
local autoStealEnabled = false

local function neutralizePrompt(prompt)
    if not promptOriginals[prompt] then
        promptOriginals[prompt] = {
            hold = prompt.HoldDuration,
            los  = prompt.RequiresLineOfSight,
        }
    end
    pcall(function()
        prompt.HoldDuration = 0
        prompt.RequiresLineOfSight = false
    end)
end

local function restoreAllPrompts()
    for prompt, data in pairs(promptOriginals) do
        if prompt and prompt.Parent then
            pcall(function()
                prompt.HoldDuration = data.hold
                prompt.RequiresLineOfSight = data.los
            end)
        end
    end
    promptOriginals = setmetatable({}, { __mode = "k" })
end

local function scanAllPrompts()
    for _, obj in ipairs(game:GetDescendants()) do
        if obj:IsA("ProximityPrompt") then
            neutralizePrompt(obj)
        end
    end
end

track(ProximityPromptService.PromptShown:Connect(function(prompt)
    if autoStealEnabled then
        neutralizePrompt(prompt)
    end
end))

track(game.DescendantAdded:Connect(function(obj)
    if autoStealEnabled and obj:IsA("ProximityPrompt") then
        task.defer(neutralizePrompt, obj)
    end
end))

--=========================================================--
--                       ROOT GUI                          --
--=========================================================--
local screenGui = Instance.new("ScreenGui")
screenGui.Name = "GlassOverlayUI"
screenGui.ResetOnSpawn = false
screenGui.IgnoreGuiInset = true
screenGui.ZIndexBehavior = Enum.ZIndexBehavior.Sibling
screenGui.Parent = playerGui

local toggleBtn = Instance.new("TextButton")
toggleBtn.Name = "Toggle"
toggleBtn.Size = UDim2.new(0, 120, 0, 44)
toggleBtn.Position = UDim2.new(0, 20, 0.38, 0)
toggleBtn.BackgroundColor3 = Theme.Glass
toggleBtn.Text = "◈  MENU"
toggleBtn.TextColor3 = Theme.Accent
toggleBtn.Font = FONT_BOLD
toggleBtn.TextSize = 15
toggleBtn.AutoButtonColor = false
toggleBtn.Active = true
toggleBtn.Parent = screenGui
corner(toggleBtn, 12)
stroke(toggleBtn, Theme.Accent, 1.5, 0.2)
grad(toggleBtn, Theme.Glass, Theme.GlassAlt, 90)

local mainFrame = Instance.new("Frame")
mainFrame.Name = "MainFrame"
mainFrame.Size = UDim2.new(0, 400, 0, 640)
mainFrame.Position = UDim2.new(0.5, -200, 0.5, -320)
mainFrame.BackgroundColor3 = Theme.Glass
mainFrame.BorderSizePixel = 0
mainFrame.Active = true
mainFrame.ClipsDescendants = true
mainFrame.Parent = screenGui
corner(mainFrame, 16)
stroke(mainFrame, Theme.Border, 1, 0.25)
grad(mainFrame, Color3.fromRGB(24, 26, 38), Color3.fromRGB(14, 15, 24), 90)

local rail = Instance.new("Frame")
rail.Size = UDim2.new(0, 4, 1, -32)
rail.Position = UDim2.new(0, 10, 0, 16)
rail.BackgroundColor3 = Theme.Accent
rail.BorderSizePixel = 0
rail.Parent = mainFrame
corner(rail, 2)
grad(rail, Theme.Accent, Theme.Accent2, 90)

local header = Instance.new("Frame")
header.Name = "Header"
header.Size = UDim2.new(1, 0, 0, 64)
header.BackgroundColor3 = Theme.GlassAlt
header.BorderSizePixel = 0
header.Parent = mainFrame
corner(header, 16)

local headerFill = Instance.new("Frame")
headerFill.Size = UDim2.new(1, 0, 0, 16)
headerFill.Position = UDim2.new(0, 0, 1, -16)
headerFill.BackgroundColor3 = Theme.GlassAlt
headerFill.BorderSizePixel = 0
headerFill.Parent = header

local titleLbl = Instance.new("TextLabel")
titleLbl.Size = UDim2.new(1, -60, 0, 26)
titleLbl.Position = UDim2.new(0, 26, 0, 10)
titleLbl.BackgroundTransparency = 1
titleLbl.Text = "RON  GWAFU"
titleLbl.TextColor3 = Theme.Text
titleLbl.Font = FONT_BLACK
titleLbl.TextSize = 20
titleLbl.TextXAlignment = Enum.TextXAlignment.Left
titleLbl.Parent = header
grad(titleLbl, Theme.Accent, Theme.Accent2, 0)

local subLbl = Instance.new("TextLabel")
subLbl.Size = UDim2.new(1, -60, 0, 14)
subLbl.Position = UDim2.new(0, 28, 0, 36)
subLbl.BackgroundTransparency = 1
subLbl.Text = "utility suite  ·  v2.5"
subLbl.TextColor3 = Theme.TextDim
subLbl.Font = FONT_REG
subLbl.TextSize = 11
subLbl.TextXAlignment = Enum.TextXAlignment.Left
subLbl.Parent = header

local statusDot = Instance.new("Frame")
statusDot.Size = UDim2.new(0, 8, 0, 8)
statusDot.Position = UDim2.new(0, 14, 0, 28)
statusDot.BackgroundColor3 = Theme.Success
statusDot.BorderSizePixel = 0
statusDot.Parent = header
corner(statusDot, 4)

local closeBtn = Instance.new("TextButton")
closeBtn.Size = UDim2.new(0, 32, 0, 32)
closeBtn.Position = UDim2.new(1, -44, 0, 16)
closeBtn.BackgroundColor3 = Theme.Card
closeBtn.Text = "✕"
closeBtn.TextColor3 = Theme.Text
closeBtn.Font = FONT_BOLD
closeBtn.TextSize = 16
closeBtn.AutoButtonColor = false
closeBtn.Parent = header
corner(closeBtn, 8)
stroke(closeBtn, Theme.Danger, 1, 0.4)

closeBtn.MouseEnter:Connect(function()
    TweenService:Create(closeBtn, TweenInfo.new(0.15), { BackgroundColor3 = Theme.Danger }):Play()
end)
closeBtn.MouseLeave:Connect(function()
    TweenService:Create(closeBtn, TweenInfo.new(0.15), { BackgroundColor3 = Theme.Card }):Play()
end)

local body = Instance.new("ScrollingFrame")
body.Size = UDim2.new(1, -44, 1, -80)
body.Position = UDim2.new(0, 26, 0, 72)
body.BackgroundTransparency = 1
body.BorderSizePixel = 0
body.ScrollBarThickness = 4
body.ScrollBarImageColor3 = Theme.Accent
body.CanvasSize = UDim2.new(0, 0, 0, 0)
body.AutomaticCanvasSize = Enum.AutomaticSize.Y
body.Parent = mainFrame

local bodyLayout = Instance.new("UIListLayout")
bodyLayout.Padding = UDim.new(0, 10)
bodyLayout.SortOrder = Enum.SortOrder.LayoutOrder
bodyLayout.Parent = body

local function sectionHeader(text, order)
    local holder = Instance.new("Frame")
    holder.Size = UDim2.new(1, -4, 0, 22)
    holder.BackgroundTransparency = 1
    holder.LayoutOrder = order
    holder.Parent = body

    local dot = Instance.new("Frame")
    dot.Size = UDim2.new(0, 6, 0, 6)
    dot.Position = UDim2.new(0, 2, 0.5, -3)
    dot.BackgroundColor3 = Theme.Accent3
    dot.BorderSizePixel = 0
    dot.Parent = holder
    corner(dot, 3)

    local lbl = Instance.new("TextLabel")
    lbl.Size = UDim2.new(1, -20, 1, 0)
    lbl.Position = UDim2.new(0, 18, 0, 0)
    lbl.BackgroundTransparency = 1
    lbl.Text = string.upper(text)
    lbl.TextColor3 = Theme.TextDim
    lbl.Font = FONT_BOLD
    lbl.TextSize = 11
    lbl.TextXAlignment = Enum.TextXAlignment.Left
    lbl.Parent = holder
    return holder
end

local function cardButton(text, accent, order, height)
    local btn = Instance.new("TextButton")
    btn.Size = UDim2.new(1, -4, 0, height or 46)
    btn.BackgroundColor3 = Theme.Card
    btn.Text = ""
    btn.AutoButtonColor = false
    btn.LayoutOrder = order
    btn.Parent = body
    corner(btn, 10)
    local s = stroke(btn, Theme.Border, 1, 0.55)

    local bar = Instance.new("Frame")
    bar.Size = UDim2.new(0, 3, 0, 22)
    bar.Position = UDim2.new(0, 12, 0.5, -11)
    bar.BackgroundColor3 = accent
    bar.BorderSizePixel = 0
    bar.Parent = btn
    corner(bar, 2)

    local label = Instance.new("TextLabel")
    label.Size = UDim2.new(1, -40, 1, 0)
    label.Position = UDim2.new(0, 26, 0, 0)
    label.BackgroundTransparency = 1
    label.Text = text
    label.TextColor3 = Theme.Text
    label.Font = FONT_MED
    label.TextSize = 14
    label.TextXAlignment = Enum.TextXAlignment.Left
    label.Parent = btn

    btn.MouseEnter:Connect(function()
        TweenService:Create(btn, TweenInfo.new(0.15), { BackgroundColor3 = Theme.CardHover }):Play()
        TweenService:Create(s,  TweenInfo.new(0.15), { Transparency = 0.15, Color = accent }):Play()
    end)
    btn.MouseLeave:Connect(function()
        TweenService:Create(btn, TweenInfo.new(0.15), { BackgroundColor3 = Theme.Card }):Play()
        TweenService:Create(s,  TweenInfo.new(0.15), { Transparency = 0.55, Color = Theme.Border }):Play()
    end)

    return btn, label, bar
end

local function chipButton(text, accent, parent, posX, width)
    local chip = Instance.new("TextButton")
    chip.Size = UDim2.new(0, width or 68, 1, 0)
    chip.Position = UDim2.new(0, posX, 0, 0)
    chip.BackgroundColor3 = Theme.GlassAlt
    chip.Text = text
    chip.TextColor3 = Theme.Text
    chip.Font = FONT_MED
    chip.TextSize = 12
    chip.AutoButtonColor = false
    chip.Parent = parent
    corner(chip, 8)
    local s = stroke(chip, Theme.Border, 1, 0.5)

    chip.MouseEnter:Connect(function()
        TweenService:Create(chip, TweenInfo.new(0.12), { BackgroundColor3 = Theme.CardHover }):Play()
        TweenService:Create(s, TweenInfo.new(0.12), { Color = accent, Transparency = 0.2 }):Play()
    end)
    chip.MouseLeave:Connect(function()
        TweenService:Create(chip, TweenInfo.new(0.12), { BackgroundColor3 = Theme.GlassAlt }):Play()
        TweenService:Create(s, TweenInfo.new(0.12), { Color = Theme.Border, Transparency = 0.5 }):Play()
    end)
    return chip
end

local function makeDraggable(frame, handle)
    handle = handle or frame
    local dragging, dragInput, dragStart, startPos = false, nil, nil, nil
    track(handle.InputBegan:Connect(function(input)
        if input.UserInputType == Enum.UserInputType.MouseButton1
        or input.UserInputType == Enum.UserInputType.Touch then
            dragging = true
            dragStart = input.Position
            startPos  = frame.Position
            track(input.Changed:Connect(function()
                if input.UserInputState == Enum.UserInputState.End then
                    dragging = false
                end
            end))
        end
    end))
    track(handle.InputChanged:Connect(function(input)
        if input.UserInputType == Enum.UserInputType.MouseMovement
        or input.UserInputType == Enum.UserInputType.Touch then
            dragInput = input
        end
    end))
    track(UserInputService.InputChanged:Connect(function(input)
        if input == dragInput and dragging then
            local d = input.Position - dragStart
            frame.Position = UDim2.new(
                startPos.X.Scale, startPos.X.Offset + d.X,
                startPos.Y.Scale, startPos.Y.Offset + d.Y
            )
        end
    end))
end
makeDraggable(mainFrame, header)
makeDraggable(toggleBtn)

track(toggleBtn.MouseButton1Click:Connect(function()
    if SHUTDOWN then return end
    mainFrame.Visible = not mainFrame.Visible
end))

--=========================================================--
--              CONFIRM CLOSE DIALOG (Yes / No)            --
--=========================================================--
local confirmOverlay = Instance.new("Frame")
confirmOverlay.Size = UDim2.new(1, 0, 1, 0)
confirmOverlay.BackgroundColor3 = Color3.fromRGB(0, 0, 0)
confirmOverlay.BackgroundTransparency = 0.5
confirmOverlay.BorderSizePixel = 0
confirmOverlay.Visible = false
confirmOverlay.ZIndex = 100
confirmOverlay.Parent = mainFrame

local confirmBox = Instance.new("Frame")
confirmBox.Size = UDim2.new(0, 320, 0, 170)
confirmBox.Position = UDim2.new(0.5, -160, 0.5, -85)
confirmBox.BackgroundColor3 = Theme.Glass
confirmBox.BorderSizePixel = 0
confirmBox.ZIndex = 101
confirmBox.Parent = confirmOverlay
corner(confirmBox, 14)
stroke(confirmBox, Theme.Danger, 1.5, 0.2)
grad(confirmBox, Color3.fromRGB(28, 24, 34), Color3.fromRGB(16, 14, 22), 90)

local confirmTitle = Instance.new("TextLabel")
confirmTitle.Size = UDim2.new(1, -32, 0, 28)
confirmTitle.Position = UDim2.new(0, 16, 0, 18)
confirmTitle.BackgroundTransparency = 1
confirmTitle.Text = "⚠  Close & Unload?"
confirmTitle.TextColor3 = Theme.Danger
confirmTitle.Font = FONT_BOLD
confirmTitle.TextSize = 17
confirmTitle.TextXAlignment = Enum.TextXAlignment.Left
confirmTitle.ZIndex = 102
confirmTitle.Parent = confirmBox

local confirmDesc = Instance.new("TextLabel")
confirmDesc.Size = UDim2.new(1, -32, 0, 56)
confirmDesc.Position = UDim2.new(0, 16, 0, 50)
confirmDesc.BackgroundTransparency = 1
confirmDesc.Text = "This will completely remove the cheat, restore\nall prompts, reset your humanoid, and delete\nthe GUI. Are you sure?"
confirmDesc.TextColor3 = Theme.TextDim
confirmDesc.Font = FONT_REG
confirmDesc.TextSize = 12
confirmDesc.TextWrapped = true
confirmDesc.TextXAlignment = Enum.TextXAlignment.Left
confirmDesc.TextYAlignment = Enum.TextYAlignment.Top
confirmDesc.ZIndex = 102
confirmDesc.Parent = confirmBox

local yesBtn = Instance.new("TextButton")
yesBtn.Size = UDim2.new(0, 140, 0, 38)
yesBtn.Position = UDim2.new(0, 16, 1, -54)
yesBtn.BackgroundColor3 = Theme.Danger
yesBtn.Text = "YES  •  Unload"
yesBtn.TextColor3 = Theme.Text
yesBtn.Font = FONT_BOLD
yesBtn.TextSize = 13
yesBtn.AutoButtonColor = false
yesBtn.ZIndex = 102
yesBtn.Parent = confirmBox
corner(yesBtn, 8)
stroke(yesBtn, Theme.Danger, 1, 0)

local noBtn = Instance.new("TextButton")
noBtn.Size = UDim2.new(0, 140, 0, 38)
noBtn.Position = UDim2.new(1, -156, 1, -54)
noBtn.BackgroundColor3 = Theme.Card
noBtn.Text = "NO  •  Cancel"
noBtn.TextColor3 = Theme.Text
noBtn.Font = FONT_BOLD
noBtn.TextSize = 13
noBtn.AutoButtonColor = false
noBtn.ZIndex = 102
noBtn.Parent = confirmBox
corner(noBtn, 8)
stroke(noBtn, Theme.Border, 1, 0.4)

noBtn.MouseEnter:Connect(function()
    TweenService:Create(noBtn, TweenInfo.new(0.12), { BackgroundColor3 = Theme.CardHover }):Play()
end)
noBtn.MouseLeave:Connect(function()
    TweenService:Create(noBtn, TweenInfo.new(0.12), { BackgroundColor3 = Theme.Card }):Play()
end)
yesBtn.MouseEnter:Connect(function()
    TweenService:Create(yesBtn, TweenInfo.new(0.12), { BackgroundColor3 = Color3.fromRGB(255, 100, 120) }):Play()
end)
yesBtn.MouseLeave:Connect(function()
    TweenService:Create(yesBtn, TweenInfo.new(0.12), { BackgroundColor3 = Theme.Danger }):Play()
end)

local function showConfirm()
    confirmOverlay.Visible = true
    confirmOverlay.BackgroundTransparency = 1
    confirmBox.Size = UDim2.new(0, 280, 0, 140)
    confirmBox.Position = UDim2.new(0.5, -140, 0.5, -70)
    TweenService:Create(confirmOverlay, TweenInfo.new(0.18), { BackgroundTransparency = 0.5 }):Play()
    TweenService:Create(confirmBox, TweenInfo.new(0.22, Enum.EasingStyle.Back, Enum.EasingDirection.Out), {
        Size = UDim2.new(0, 320, 0, 170),
        Position = UDim2.new(0.5, -160, 0.5, -85),
    }):Play()
end

local function hideConfirm()
    TweenService:Create(confirmOverlay, TweenInfo.new(0.15), { BackgroundTransparency = 1 }):Play()
    local t = TweenService:Create(confirmBox, TweenInfo.new(0.15), {
        Size = UDim2.new(0, 280, 0, 140),
        Position = UDim2.new(0.5, -140, 0.5, -70),
    })
    t:Play()
    t.Completed:Connect(function()
        confirmOverlay.Visible = false
    end)
end

track(closeBtn.MouseButton1Click:Connect(showConfirm))
track(noBtn.MouseButton1Click:Connect(hideConfirm))

--=========================================================--
--                    SECTION: ACTIONS                     --
--=========================================================--
sectionHeader("Actions", 1)

local bypassBtn, bypassLbl = cardButton("🛡   Replace Humanoid : OFF", Theme.Disabled, 2)
local moveBtn    = cardButton("🎯   Move to Stand",                   Theme.Accent,  3)
local respawnBtn = cardButton("♻   Respawn Character",               Theme.Warning, 4)

--=========================================================--
--                   SECTION: AUTO-STEAL                   --
--=========================================================--
sectionHeader("Auto Steal", 5)

local autoStealBtn, autoStealLbl = cardButton(
    "◌   Auto Steal : OFF   (0s hold duration)",
    Theme.Disabled, 6
)

--=========================================================--
--                   SECTION: TP WALK                      --
--=========================================================--
sectionHeader("Teleport Walk", 7)

local tpWalkBtn, tpWalkLbl = cardButton("⚡   TP Walk : OFF", Theme.Disabled, 8)

local speedLbl = Instance.new("TextLabel")
speedLbl.Size = UDim2.new(1, -4, 0, 18)
speedLbl.BackgroundTransparency = 1
speedLbl.Text = "Speed : 50 studs/s"
speedLbl.TextColor3 = Theme.TextDim
speedLbl.Font = FONT_REG
speedLbl.TextSize = 12
speedLbl.TextXAlignment = Enum.TextXAlignment.Left
speedLbl.LayoutOrder = 9
speedLbl.Parent = body
pad(speedLbl, 0, 0, 4, 0)

local speedBar = Instance.new("Frame")
speedBar.Size = UDim2.new(1, -4, 0, 22)
speedBar.BackgroundColor3 = Theme.GlassAlt
speedBar.BorderSizePixel = 0
speedBar.LayoutOrder = 10
speedBar.Parent = body
corner(speedBar, 11)
stroke(speedBar, Theme.Border, 1, 0.5)

local speedFill = Instance.new("Frame")
speedFill.Size = UDim2.new(0.16, 0, 1, 0)
speedFill.BackgroundColor3 = Theme.Accent
speedFill.BorderSizePixel = 0
speedFill.Parent = speedBar
corner(speedFill, 11)
grad(speedFill, Theme.Accent, Theme.Accent2, 0)

local speedHandle = Instance.new("TextButton")
speedHandle.Size = UDim2.new(0, 18, 0, 18)
speedHandle.Position = UDim2.new(0.16, -9, 0.5, -9)
speedHandle.BackgroundColor3 = Color3.fromRGB(255, 255, 255)
speedHandle.Text = ""
speedHandle.AutoButtonColor = false
speedHandle.Active = true
speedHandle.Parent = speedBar
corner(speedHandle, 9)
stroke(speedHandle, Theme.Accent, 2, 0)

local presetRow = Instance.new("Frame")
presetRow.Size = UDim2.new(1, -4, 0, 30)
presetRow.BackgroundTransparency = 1
presetRow.LayoutOrder = 11
presetRow.Parent = body

local presetValues = {50, 300, 800, 1500, 2500}
local presetLabels = {"50", "300", "800", "1500", "MAX"}
local chipWidths   = {52, 60, 60, 68, 60}
local presetChips  = {}

local cursorX = 0
for i = 1, #presetLabels do
    local w = chipWidths[i]
    local chip = chipButton(presetLabels[i], Theme.Accent, presetRow, cursorX, w)
    presetChips[i] = chip
    cursorX = cursorX + w + 6
end

local stopBtn = Instance.new("TextButton")
stopBtn.Size = UDim2.new(0, 96, 0, 34)
stopBtn.Position = UDim2.new(1, -134, 0, 16)
stopBtn.BackgroundColor3 = Theme.Danger
stopBtn.Text = "■  STOP"
stopBtn.TextColor3 = Theme.Text
stopBtn.Font = FONT_BOLD
stopBtn.TextSize = 13
stopBtn.AutoButtonColor = false
stopBtn.Visible = false
stopBtn.ZIndex = 5
stopBtn.Parent = header
corner(stopBtn, 8)
stroke(stopBtn, Theme.Danger, 1.5, 0)

--=========================================================--
--                  ACTIONS LOGIC                          --
--=========================================================--
local BOOST_SPEED = 250
local boostState = {
    active   = false,
    original = nil,
}

local function getHumanoid()
    local c = localPlayer.Character
    if not c then return nil end
    return c:FindFirstChildOfClass("Humanoid")
end

local function swapHumanoid(walkSpeed, jumpPower, jumpHeight, useJumpPower)
    local character = localPlayer.Character
    if not character then return false end

    local old = character:FindFirstChildOfClass("Humanoid")
    if not old then return false end

    local oldHealth    = old.Health
    local oldMaxHealth = old.MaxHealth
    local oldNameDisp  = old.DisplayDistanceType

    old:Destroy()

    local new = Instance.new("Humanoid")
    new.WalkSpeed    = walkSpeed
    new.UseJumpPower = useJumpPower
    if useJumpPower then
        new.JumpPower = jumpPower
    else
        new.JumpHeight = jumpHeight
    end
    new.MaxHealth = oldMaxHealth
    new.Health    = math.min(oldHealth, oldMaxHealth)
    new.DisplayDistanceType = oldNameDisp
    new.Parent = character

    return true
end

local function applyBoost()
    local humanoid = getHumanoid()
    if not humanoid or humanoid.Health <= 0 then
        notify("Bypass", "No active character", 2)
        return
    end

    -- snapshot ORIGINAL stats so OFF can restore them
    boostState.original = {
        walkSpeed    = humanoid.WalkSpeed,
        jumpPower    = humanoid.JumpPower,
        jumpHeight   = humanoid.JumpHeight,
        useJumpPower = humanoid.UseJumpPower,
    }

    local ok = swapHumanoid(
        BOOST_SPEED,
        math.max(boostState.original.jumpPower, 80),
        math.max(boostState.original.jumpHeight, 12),
        boostState.original.useJumpPower
    )

    if not ok then
        boostState.original = nil
        notify("Bypass", "Failed to replace", 2)
        return
    end

    boostState.active = true
    bypassLbl.Text = ("🛡   Replace Humanoid : ON  (%d)"):format(BOOST_SPEED)
    bypassLbl.TextColor3 = Theme.Success
    bypassBtn.BackgroundColor3 = Color3.fromRGB(26, 46, 40)
    notify("Bypass", ("WalkSpeed set to %d"):format(BOOST_SPEED), 2)
end

local function removeBoost()
    local humanoid = getHumanoid()
    if not humanoid then
        boostState.active   = false
        boostState.original = nil
        bypassLbl.Text = "🛡   Replace Humanoid : OFF"
        bypassLbl.TextColor3 = Theme.Text
        bypassBtn.BackgroundColor3 = Theme.Card
        return
    end

    -- Use saved original. If we don't have it, fall back to Roblox defaults.
    local snap = boostState.original or {
        walkSpeed    = 16,
        jumpPower    = 50,
        jumpHeight   = 7.2,
        useJumpPower = true,
    }

    local ok = swapHumanoid(
        snap.walkSpeed,
        snap.jumpPower,
        snap.jumpHeight,
        snap.useJumpPower
    )

    if not ok then
        notify("Bypass", "Failed to restore", 2)
        return
    end

    boostState.active   = false
    boostState.original = nil
    bypassLbl.Text = "🛡   Replace Humanoid : OFF"
    bypassLbl.TextColor3 = Theme.Text
    bypassBtn.BackgroundColor3 = Theme.Card
    notify("Bypass", ("Restored to normal (%d studs/s)"):format(snap.walkSpeed), 2)
end

track(bypassBtn.MouseButton1Click:Connect(function()
    if SHUTDOWN then return end
    if boostState.active then
        removeBoost()
    else
        applyBoost()
    end
end))

track(localPlayer.CharacterAdded:Connect(function()
    boostState.active   = false
    boostState.original = nil
    if bypassLbl then
        bypassLbl.Text = "🛡   Replace Humanoid : OFF"
        bypassLbl.TextColor3 = Theme.Text
    end
    if bypassBtn then
        bypassBtn.BackgroundColor3 = Theme.Card
    end
end))

local activeTween, tweenToken = nil, 0

track(moveBtn.MouseButton1Click:Connect(function()
    if SHUTDOWN then return end
    if activeTween then activeTween:Cancel() end

    local character = localPlayer.Character
    if not character then return end
    local rootPart = character:FindFirstChild("HumanoidRootPart")
    if not rootPart then return end

    local target = CFrame.new(
        544.577637, 92.0762939, -364.869049,
        -1, 0, 0,
         0, 1, 0,
         0, 0, -1
    )

    tweenToken = tweenToken + 1
    local myToken = tweenToken

    activeTween = TweenService:Create(
        rootPart,
        TweenInfo.new(7, Enum.EasingStyle.Linear),
        { CFrame = target }
    )

    stopBtn.Visible = true
    activeTween.Completed:Connect(function()
        if myToken == tweenToken then
            stopBtn.Visible = false
            activeTween = nil
        end
    end)
    activeTween:Play()
end))

track(stopBtn.MouseButton1Click:Connect(function()
    tweenToken = tweenToken + 1
    if activeTween then
        activeTween:Cancel()
        activeTween = nil
    end
    stopBtn.Visible = false
end))

track(respawnBtn.MouseButton1Click:Connect(function()
    if SHUTDOWN then return end
    local character = localPlayer.Character
    if not character then return end
    local humanoid = character:FindFirstChildOfClass("Humanoid")
    if humanoid then humanoid.Health = 0 end
end))

track(autoStealBtn.MouseButton1Click:Connect(function()
    if SHUTDOWN then return end
    autoStealEnabled = not autoStealEnabled

    if autoStealEnabled then
        autoStealLbl.Text = "◉   Auto Steal : ON   (0s hold duration)"
        autoStealLbl.TextColor3 = Theme.Success
        autoStealBtn.BackgroundColor3 = Color3.fromRGB(26, 46, 40)
        scanAllPrompts()
        notify("Auto Steal", "All prompts now have 0s hold", 2)
    else
        autoStealLbl.Text = "◌   Auto Steal : OFF   (0s hold duration)"
        autoStealLbl.TextColor3 = Theme.Text
        autoStealBtn.BackgroundColor3 = Theme.Card
        restoreAllPrompts()
        notify("Auto Steal", "Disabled — prompts restored", 2)
    end
end))

--=========================================================--
--                    TP WALK LOGIC                        --
--=========================================================--
local tpWalkEnabled = false
local tpSpeed = 50

local function setHumanoidFloating(humanoid, floating)
    if not humanoid or humanoid.Health <= 0 then return end
    if floating then
        pcall(function()
            humanoid:SetStateEnabled(Enum.HumanoidStateType.Freefall, false)
            humanoid:SetStateEnabled(Enum.HumanoidStateType.Landed, false)
            humanoid:SetStateEnabled(Enum.HumanoidStateType.Running, false)
            humanoid:SetStateEnabled(Enum.HumanoidStateType.RunningNoPhysics, false)
            humanoid:SetStateEnabled(Enum.HumanoidStateType.Climbing, false)
            humanoid:SetStateEnabled(Enum.HumanoidStateType.Swimming, false)
        end)
    else
        pcall(function()
            for _, st in ipairs({
                Enum.HumanoidStateType.Freefall,
                Enum.HumanoidStateType.Landed,
                Enum.HumanoidStateType.Running,
                Enum.HumanoidStateType.RunningNoPhysics,
                Enum.HumanoidStateType.Climbing,
                Enum.HumanoidStateType.Swimming,
                Enum.HumanoidStateType.GettingUp,
                Enum.HumanoidStateType.Jumping,
            }) do
                humanoid:SetStateEnabled(st, true)
            end
            humanoid:ChangeState(Enum.HumanoidStateType.Freefall)
        end)
    end
end

-- Smoothly drop the character to the nearest ground below them.
-- Called when TP Walk turns OFF so you don't hang mid-air.
local function landOnGround()
    local character = localPlayer.Character
    if not character then return end
    local rootPart = character:FindFirstChild("HumanoidRootPart")
    local humanoid = character:FindFirstChildOfClass("Humanoid")
    if not rootPart or not humanoid then return end

    -- Release any leftover velocity so gravity actually pulls us down
    pcall(function()
        rootPart.AssemblyLinearVelocity = Vector3.new(0, -50, 0)
    end)

    -- Raycast down to find the floor
    local params = RaycastParams.new()
    params.FilterType = Enum.RaycastFilterType.Exclude
    params.FilterDescendantsInstances = { character }
    params.IgnoreWater = false

    local origin    = rootPart.Position
    local direction = Vector3.new(0, -500, 0)
    local result    = workspace:Raycast(origin, direction, params)

    if result then
        -- Tween smoothly down to the floor (a small offset so we stand on it)
        local targetPos   = result.Position + Vector3.new(0, 3, 0)
        local targetCFrame = CFrame.new(targetPos, targetPos + rootPart.CFrame.LookVector)

        local tween = TweenService:Create(
            rootPart,
            TweenInfo.new(0.35, Enum.EasingStyle.Quad, Enum.EasingDirection.Out),
            { CFrame = targetCFrame }
        )
        tween:Play()
    end

    -- Ensure the humanoid goes into Freefall so gravity resumes
    pcall(function()
        humanoid:ChangeState(Enum.HumanoidStateType.Freefall)
    end)
end

local function onCharacterAdded(character)
    if SHUTDOWN then return end
    local humanoid = character:WaitForChild("Humanoid", 5)
    if not humanoid then return end
    if tpWalkEnabled then
        setHumanoidFloating(humanoid, true)
    end
end

track(localPlayer.CharacterAdded:Connect(onCharacterAdded))
if localPlayer.Character then
    task.spawn(onCharacterAdded, localPlayer.Character)
end

track(tpWalkBtn.MouseButton1Click:Connect(function()
    if SHUTDOWN then return end
    tpWalkEnabled = not tpWalkEnabled
    local character = localPlayer.Character
    local humanoid = character and character:FindFirstChildOfClass("Humanoid")

    if tpWalkEnabled then
        tpWalkLbl.Text = "⚡   TP Walk : ON"
        tpWalkLbl.TextColor3 = Theme.Success
        tpWalkBtn.BackgroundColor3 = Color3.fromRGB(26, 46, 40)
        if humanoid then
            setHumanoidFloating(humanoid, true)
        end
    else
        tpWalkLbl.Text = "⚡   TP Walk : OFF"
        tpWalkLbl.TextColor3 = Theme.Text
        tpWalkBtn.BackgroundColor3 = Theme.Card

        if humanoid then
            setHumanoidFloating(humanoid, false)
        end
        -- Force us down to the ground so we don't hang mid-air
        landOnGround()
    end
end))

--=========================================================--
--                    SPEED SLIDER (LINEAR)                --
--=========================================================--
local draggingSpeed = false
local MIN_SPEED, MAX_SPEED = 1, 2500

local function setSpeedFromRelative(rel)
    rel = math.clamp(rel, 0, 1)
    tpSpeed = math.floor(MIN_SPEED + rel * (MAX_SPEED - MIN_SPEED))
    speedFill.Size       = UDim2.new(rel, 0, 1, 0)
    speedHandle.Position = UDim2.new(rel, -9, 0.5, -9)
    speedLbl.Text = ("Speed : %d studs/s"):format(tpSpeed)
end

local function setSpeedAbsolute(speed)
    speed = math.clamp(speed, MIN_SPEED, MAX_SPEED)
    local rel = (speed - MIN_SPEED) / (MAX_SPEED - MIN_SPEED)
    setSpeedFromRelative(rel)
end

local function relFromInput(input)
    return (input.Position.X - speedBar.AbsolutePosition.X) / speedBar.AbsoluteSize.X
end

track(speedBar.InputBegan:Connect(function(input)
    if input.UserInputType == Enum.UserInputType.MouseButton1
    or input.UserInputType == Enum.UserInputType.Touch then
        draggingSpeed = true
        setSpeedFromRelative(relFromInput(input))
    end
end))

track(speedHandle.InputBegan:Connect(function(input)
    if input.UserInputType == Enum.UserInputType.MouseButton1
    or input.UserInputType == Enum.UserInputType.Touch then
        draggingSpeed = true
    end
end))

track(UserInputService.InputChanged:Connect(function(input)
    if draggingSpeed and (input.UserInputType == Enum.UserInputType.MouseMovement
    or input.UserInputType == Enum.UserInputType.Touch) then
        setSpeedFromRelative(relFromInput(input))
    end
end))

track(UserInputService.InputEnded:Connect(function(input)
    if input.UserInputType == Enum.UserInputType.MouseButton1
    or input.UserInputType == Enum.UserInputType.Touch then
        draggingSpeed = false
    end
end))

for i, chip in ipairs(presetChips) do
    track(chip.MouseButton1Click:Connect(function()
        if SHUTDOWN then return end
        setSpeedAbsolute(presetValues[i])
    end))
end

setSpeedAbsolute(50)

--=========================================================--
--                  TP WALK HEARTBEAT                      --
--=========================================================--
track(RunService.Heartbeat:Connect(function(dt)
    if SHUTDOWN or not tpWalkEnabled then return end

    local character = localPlayer.Character
    if not character then return end

    local humanoid = character:FindFirstChildOfClass("Humanoid")
    local rootPart = character:FindFirstChild("HumanoidRootPart")
    if not humanoid or not rootPart then return end
    if humanoid.Health <= 0 then return end
    if rootPart.Anchored then return end
    if humanoid.MoveDirection.Magnitude <= 0 then return end

    rootPart.CFrame = rootPart.CFrame + humanoid.MoveDirection * tpSpeed * dt
end))

--=========================================================--
--                 FULL SHUTDOWN HANDLER                   --
--=========================================================--
local function shutdownAll()
    if SHUTDOWN then return end

    -- Restore humanoid if boost was on
    if boostState and boostState.active then
        pcall(removeBoost)
    end

    -- Turn off TP walk and drop to ground before destroying GUI
    if tpWalkEnabled then
        tpWalkEnabled = false
        local character = localPlayer.Character
        local humanoid = character and character:FindFirstChildOfClass("Humanoid")
        if humanoid then
            setHumanoidFloating(humanoid, false)
        end
        pcall(landOnGround)
    end

    SHUTDOWN = true

    tweenToken = tweenToken + 1
    if activeTween then
        pcall(function() activeTween:Cancel() end)
        activeTween = nil
    end

    restoreAllPrompts()
    disconnectAll()
    pcall(function() screenGui:Destroy() end)

    notify("Glass UI", "Unloaded. Cheat removed.", 3)
end

track(yesBtn.MouseButton1Click:Connect(shutdownAll))
_G.GlassUI_Shutdown = shutdownAll