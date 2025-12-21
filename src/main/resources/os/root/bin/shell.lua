function execute(current_path, arguments)
    System:setRawInput(true)

    local command_arr = {}
    local cursorPos = #command_arr

    while true do
        System:clear()
        System:print(System:getVersion() .. "\n")
        System:print(assemble(command_arr))

        local input = System:awaitInput()

        -- Left Arrow
        if input == 37 then
            cursorPos = cursorPos - 1
            if cursorPos <= 0 then
                cursorPos = 1
            end
            goto continue
        end

        -- Right Arrow
        if input == 39 then
            cursorPos = cursorPos + 1
            if cursorPos > #command_arr then
                cursorPos = #command_arr
            end
            goto continue
        end

        if input == 8 then
            table.remove(command_arr, #command_arr - cursorPos)
            cursorPos = cursorPos - 1
            if cursorPos <= 0 then
                cursorPos = 1
            end
            goto continue
        end

        if System:isModifierKey(input) then
            goto continue
        end

        command_arr[#command_arr + 1] = input
        cursorPos = cursorPos + 1
        if cursorPos > #command_arr then
            cursorPos = #command_arr
        end

        ::continue::
    end

    System:setRawInput(false)
end

function assemble(table)
    local str = ""
    for _, i in ipairs(table) do
        str = str .. i
    end

    return str
end