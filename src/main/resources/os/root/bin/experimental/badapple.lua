frames = {}

function execute(current_path, arguments)
    local binFile = System:getFile(current_path .. "./badapple.bin")
    local data = binFile:getData()



    local output = ""
    for i, byte in ipairs(data) do
        local highNibble = math.floor(byte / 16)
        local lowNibble  = byte % 16

        output = comp(output, highNibble, i, frames)
        output = comp(output, lowNibble, i, frames)
    end

    System:clear()

    for _, string in ipairs(frames) do
        System:print(string)
        System:sleep(35)
        System:clear()
    end
end


function comp(output, value, index, frames)
    local pixelMap = {
        [0] = "#",
        [1] = "8",
        [2] = "&",
        [3] = "*",
        [4] = ":",
        [5] = ".",
        [6] = " "
    }

    if value == 10 then
        return output .. "\n"
    elseif value > 10 then
        frames[math.ceil(index)] = output
        return ""
    else
        return output .. (pixelMap[value] or " ") .. (pixelMap[value] or " ")
    end
end
