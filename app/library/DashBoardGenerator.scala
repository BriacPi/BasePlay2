package library

import breeze.linalg.unique
import models._

object DashBoardGenerator {
  def getDashBoardsForAgence(caisse: String, groupe: String , agence: String  ):List[DashBoard]={
    val suspectRows = SuspectRow.all().filter(suspectRow => {
      caisse==suspectRow.caisse && groupe==suspectRow.groupe &&  agence==suspectRow.agence
    })
    val pdvList= suspectRows.map(_.pdv).distinct
    pdvList.map(pdv => createDashBoard(caisse, groupe , agence , pdv,suspectRows ))
  }

  def getDashBoardsForGroupe(caisse: String, groupe: String ):List[DashBoard]={
    val suspectRows = SuspectRow.all().filter(suspectRow => {
      caisse==suspectRow.caisse && groupe==suspectRow.groupe
    })
    val agenceList= suspectRows.map(_.agence).distinct
    agenceList.map(agence => createDashBoard(caisse, groupe , agence,suspectRows ))
  }

  def getDashBoardsForCaisse(caisse: String ):List[DashBoard]={
    val suspectRows = SuspectRow.all().filter(suspectRow => {
      caisse==suspectRow.caisse
    })
    val groupeList= suspectRows.map(_.groupe).distinct
    groupeList.map(groupe => createDashBoard(caisse, groupe ,suspectRows ))
  }

  def getDashBoardsForAllCaisses():List[DashBoard]={
    val suspectRows = SuspectRow.all()
    val caisseList= suspectRows.map(_.caisse).distinct
    caisseList.map(caisse => createDashBoard(caisse ,suspectRows ))
  }

  def getDashBoardsForAll():DashBoard={
   createDashBoard()
  }




  def createDashBoard(caisse: String, groupe: String , agence: String , pdv: String,suspectRowsUnfiltered:List[SuspectRow] ): DashBoard = {
    val suspectRows = suspectRowsUnfiltered.filter(suspectRow => {
      caisse==suspectRow.caisse && groupe==suspectRow.groupe &&  agence==suspectRow.agence && pdv==suspectRow.pdv
    })
    buildDashBoardForPdv(suspectRows)
  }

  def createDashBoard(caisse: String, groupe: String , agence: String,suspectRowsUnfiltered:List[SuspectRow]  ): DashBoard = {
    val suspectRows = suspectRowsUnfiltered.filter(suspectRow => {
       caisse==suspectRow.caisse && groupe==suspectRow.groupe && agence==suspectRow.agence
    })
    buildDashBoardForAgence(suspectRows)
  }

  def createDashBoard(caisse: String, groupe: String,suspectRowsUnfiltered:List[SuspectRow]  ): DashBoard = {
    val suspectRows = suspectRowsUnfiltered.filter(suspectRow => {
      caisse==suspectRow.caisse &&groupe==suspectRow.groupe
    })
    buildDashBoardForGroupe(suspectRows)
  }

  def createDashBoard(caisse: String,suspectRowsUnfiltered:List[SuspectRow] ): DashBoard = {
    val suspectRows = suspectRowsUnfiltered.filter(suspectRow => {
 caisse==suspectRow.caisse
    }
    )
    buildDashBoardForCaisse(suspectRows)
  }

  def createDashBoard(): DashBoard = {
    val suspectRows = SuspectRow.all()
    buildDashBoardForAll(suspectRows)
  }

  def buildDashBoardForAll(suspectRows: List[SuspectRow]):DashBoard={
    val leaderBoard = createLeaderBoardForAll(suspectRows)
    val statusChart = createStatusChart(suspectRows)
    val natureChart = createNatureChart(suspectRows)

    DashBoard("Toutes les caisses","all",suspectRows.length,leaderBoard,statusChart,natureChart)
  }
  def buildDashBoardForCaisse(suspectRows: List[SuspectRow]):DashBoard={
    val leaderBoard = createLeaderBoardForCaisse(suspectRows)
    val statusChart = createStatusChart(suspectRows)
    val natureChart = createNatureChart(suspectRows)

    DashBoard(suspectRows.head.caisse,"caisse",suspectRows.length,leaderBoard,statusChart,natureChart)
  }
  def buildDashBoardForGroupe(suspectRows: List[SuspectRow]):DashBoard={
    val leaderBoard = createLeaderBoardForGroupe(suspectRows)
    val statusChart = createStatusChart(suspectRows)
    val natureChart = createNatureChart(suspectRows)

    DashBoard(suspectRows.head.groupe,"groupe",suspectRows.length,leaderBoard,statusChart,natureChart)
  }
  def buildDashBoardForAgence(suspectRows: List[SuspectRow]):DashBoard={
    val leaderBoard = createLeaderBoardForAgence(suspectRows)
    val statusChart = createStatusChart(suspectRows)
    val natureChart = createNatureChart(suspectRows)

    DashBoard(suspectRows.head.agence,"agence",suspectRows.length,leaderBoard,statusChart,natureChart)
  }
  def buildDashBoardForPdv(suspectRows: List[SuspectRow]):DashBoard={
    val leaderBoard = createLeaderBoardForPdv(suspectRows)
    val statusChart = createStatusChart(suspectRows)
    val natureChart = createNatureChart(suspectRows)

    DashBoard(suspectRows.head.pdv,"pdv",suspectRows.length,leaderBoard,statusChart,natureChart)
  }

  def createLeaderBoardForPdv( suspectRows:List[SuspectRow]):List[LeaderBoardLign] = {
      List.empty[LeaderBoardLign]
    }

  def createLeaderBoardForAgence( suspectRows:List[SuspectRow]): List[LeaderBoardLign] = {
      val underHierarchy:Map[String,List[SuspectRow]]=suspectRows.groupBy(suspectRow => suspectRow.pdv)
      underHierarchy.mapValues(suspectRows => suspectRows.count(suspectRow=>suspectRow.status!=Status.Solved).toLong).toList.map(couple => LeaderBoardLign(couple._1,couple._2))
    }
  def createLeaderBoardForGroupe(suspectRows:List[SuspectRow]): List[LeaderBoardLign] = {
    val underHierarchy:Map[String,List[SuspectRow]]=suspectRows.groupBy(suspectRow => suspectRow.agence)
    underHierarchy.mapValues(suspectRows => suspectRows.count(suspectRow=>suspectRow.status!=Status.Solved).toLong).toList.map(couple => LeaderBoardLign(couple._1,couple._2))
  }
  def createLeaderBoardForCaisse(suspectRows:List[SuspectRow]): List[LeaderBoardLign] = {
    val underHierarchy:Map[String,List[SuspectRow]]=suspectRows.groupBy(suspectRow => suspectRow.groupe)
    underHierarchy.mapValues(suspectRows => suspectRows.count(suspectRow=>suspectRow.status!=Status.Solved).toLong).toList.map(couple => LeaderBoardLign(couple._1,couple._2))
  }
  def createLeaderBoardForAll(suspectRows:List[SuspectRow]): List[LeaderBoardLign] = {
    val underHierarchy:Map[String,List[SuspectRow]]=suspectRows.groupBy(suspectRow => suspectRow.caisse)
    underHierarchy.mapValues(suspectRows => suspectRows.count(suspectRow=>suspectRow.status!=Status.Solved).toLong).toList.map(couple => LeaderBoardLign(couple._1,couple._2))
  }

  def createStatusChart(suspectRows:List[SuspectRow]): Chart ={
    val groupedByStatus:Map[String,List[SuspectRow]]=suspectRows.groupBy(suspectRow => suspectRow.status.toString)
    val chartAsMap= groupedByStatus.mapValues(suspectRows => suspectRows.length)
    val (labels,data) = chartAsMap.toList.sortBy(_._2).unzip
    Chart(labels,data,List())
  }

  def createNatureChart(suspectRows:List[SuspectRow]): Chart ={
    val groupedByNature:Map[String,List[SuspectRow]]=suspectRows.groupBy(suspectRow => suspectRow.nature.toString)
    val chartAsMap=groupedByNature.mapValues(suspectRows => suspectRows.length)
    val (labels,data) = chartAsMap.toList.sortBy(_._2).unzip
    Chart(labels,data,List())
  }

}
