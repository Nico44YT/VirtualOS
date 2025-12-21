function execute(current_path, arguments)
    System:setRawInput(true)

    while true do
        local keyCode = System:awaitInput()

        if keyCode == 27 then
            break
        end

        System:print(keyCode .. "\n")

    end

    System:setRawInput(false)
end
