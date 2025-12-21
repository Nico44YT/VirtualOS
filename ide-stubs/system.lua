---@class System
local System = {}

function System:clear() end

---@param command string
---@return boolean
function System:execute(command) end

---@param command string
---@param arguments string[]
---@return boolean
function System:execute(command, arguments) end

---@param milliseconds integer
function System:sleep(milliseconds) end

---@param output string
function System:print(output) end

---@param path string
---@return File
function System:getFile(path) end

---@param s string
---@return Directory
function System:createDirectory(s) end

---@param path string
---@return File[]
function System:listFiles(path) end

---@return string
function System:getVersion() end

---@param s string
---@return boolean
function System:createFile(s) end

---@param name string
---@return boolean
function System:isDirectoryNameValid(name) end

---@return boolean
function System:isAlternativeGraphicsDown() end

---@return boolean
function System:isCapsLockActive() end

---@return boolean
function System:isShiftDown() end

---@param path string
---@return Directory[]
function System:listDirectories(path) end

---@param keyCode integer
---@return string
function System:getCharacter(keyCode) end

---@param value boolean
function System:setRawInput(value) end

---@param name string
---@return boolean
function System:isFileNameValid(name) end

---@param code integer
---@return boolean
function System:isModifierKey(code) end

---@return boolean
function System:isControlDown() end

function System:reboot() end

---@return any
function System:awaitInput() end

---@param path string
---@return Node[]
function System:listNodes(path) end

---@param hz integer
---@param durationMs integer
function System:tone(hz, durationMs) end

---@param path string
---@return boolean
function System:setCwd(path) end

function System:resetCaret() end

---@param path string
---@return string
function System:readFile(path) end

return System
