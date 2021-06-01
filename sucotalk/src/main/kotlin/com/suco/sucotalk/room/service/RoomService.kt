package com.suco.sucotalk.room.service

import com.suco.sucotalk.chat.domain.Message
import com.suco.sucotalk.chat.service.MessageService
import com.suco.sucotalk.member.domain.Member
import com.suco.sucotalk.room.domain.Room
import com.suco.sucotalk.room.repository.RoomRepositoryImpl
import org.springframework.stereotype.Service

@Service
class RoomService(private val messageService: MessageService,
                  private val roomRepositoryImpl: RoomRepositoryImpl) {

    fun exit(member: Member, roomId: Long){
        val room = roomRepositoryImpl.findById(roomId)
        room.exit(member)
        roomRepositoryImpl.deleteMemberInRoom(room, member)
    }

    fun enter(member: Member, roomId: Long) {
        val room = roomRepositoryImpl.findById(roomId)
        room.enter(member)
        roomRepositoryImpl.insertMemberInRoom(room, member)
    }

    fun enterNewRoom(members: List<Member>) : List<Message>{
        if(members.size == 2){
            val dmRoom = findDirectRoom(members[0], members[1]) ?: createNewRoom(members)
            return messageService.findAllInRoom(dmRoom)
        }

        val dmRoom = createNewRoom(members)
        return messageService.findAllInRoom(dmRoom)
    }

    fun sendMessage(sender: Member, roomId: Long, message: String){
        sendMessage(sender, roomRepositoryImpl.findById(roomId), message)
    }

    fun sendMessage(sender: Member, room: Room, message: String){
        val message = Message(sender = sender, room = room, content = message)
        messageService.save(message)
//        socketService.send(message)
    }

    fun sendDirectMessage(sender: Member, receiver: Member, message: String) {
        val dmRoom = findDirectRoom(sender, receiver)
            ?: createNewRoom(mutableListOf(sender, receiver))

        sendMessage(sender, dmRoom, message)
    }

    private fun createNewRoom(members : List<Member>) : Room{
        return roomRepositoryImpl.save(Room(members = members))
    }

    private fun findDirectRoom(sender: Member, receiver: Member): Room? {
        val dmRooms1 = roomRepositoryImpl.findEnteredRoom(sender).filter { it.isDm() }
        val dmRooms2 = roomRepositoryImpl.findEnteredRoom(receiver).filter { it.isDm() }

        return dmRooms1.find { dmRooms2.contains(it) }
    }
}