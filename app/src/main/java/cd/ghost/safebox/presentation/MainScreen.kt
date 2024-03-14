package cd.ghost.safebox.presentation

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cd.ghost.safebox.R
import cd.ghost.safebox.core.DateFormatterUtil
import cd.ghost.safebox.domain.LaunchEvent
import cd.ghost.safebox.domain.entities.ItemFile
import cd.ghost.safebox.domain.entities.ListType
import cd.ghost.safebox.presentation.components.ProgressBar
import cd.ghost.safebox.presentation.navigation.Destinations
import cd.ghost.safebox.presentation.viewmodels.MainViewModel
import kotlinx.coroutines.launch

private const val TAG = "MainScreen"

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
    onDrawerItemClick: (destinationId: String) -> Unit
) {

    val state by viewModel.stateValue.observeAsState()
    val context = LocalContext.current
    val showPanelAndCheckBoxes = state?.showActionsPanel ?: false

    viewModel.message.LaunchEvent { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    val actionListTypeIcon =
        if (state?.listType == ListType.LIST_TYPE_COLUMN) {
            painterResource(R.drawable.ic_grid)
        } else {
            painterResource(R.drawable.ic_line)
        }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // handle back physical button. When Drawer is open by pressing back button close and than leave app or other operations.
    BackHandler(drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(stringResource(R.string.app_name), modifier = Modifier.padding(16.dp))
                Divider()
                DrawerItem(name = "Settings", icon = Icons.Default.Settings){
                    onDrawerItemClick(Destinations.SettingsScreen.label)
                }
            }
        },
        gesturesEnabled = true,
    ) {
        Scaffold(
            modifier = Modifier.padding(2.dp),
            floatingActionButton = {
                FloatingActionButton(modifier = Modifier.size(56.dp),
                    onClick = { viewModel.requestForContent() }) {
                    Icon(
                        imageVector = Icons.Filled.Add, contentDescription = null
                    )
                }
            },
            topBar = {
                AppBar(onNavigationItemClick = {
                    drawerState.apply {
                        scope.launch {
                            if (isOpen) close() else open()
                        }
                    }
                }) {
                    IconButton(onClick = {
                        viewModel.changeListType()
                    }) {
                        Icon(
                            painter = actionListTypeIcon,
                            contentDescription = null
                        )
                    }
                }
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = state?.showActionsPanel == true,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ActionButton(
                            modifier = Modifier
                                .padding(8.dp)
                                .weight(1f)
                                .clickable {
                                    viewModel.decodeSelected()
                                }, label = "Decrypt", icon = R.drawable.ic_lock_open
                        )
                        ActionButton(
                            modifier = Modifier
                                .padding(8.dp)
                                .weight(1f)
                                .clickable {
                                    viewModel.deleteSelected()
                                }, label = "Delete", icon = R.drawable.ic_trash
                        )
                        AnimatedVisibility(
                            modifier = Modifier
                                .padding(8.dp)
                                .weight(1f),
                            visible = state?.showDetailsAction == true,
                            enter = fadeIn() + expandHorizontally(
                                expandFrom = AbsoluteAlignment.Right
                            ),
                            exit = fadeOut() + shrinkHorizontally(
                                shrinkTowards = AbsoluteAlignment.Left
                            )
                        ) {
                            ActionButton(
                                modifier = Modifier.clickable {
                                    viewModel.openSelected()
                                }, label = "Open", icon = R.drawable.ic_eye
                            )
                        }
                    }
                }
            },
        ) { paddingValues ->
            val transition =
                updateTransition(targetState = showPanelAndCheckBoxes, label = null)

            val visibility by transition.animateFloat(
                label = "animate visibility"
            ) {
                if (it) 1f else 0f
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                val list = state?.data ?: emptyList()
                val lazyColumnState = rememberLazyListState()
                if (list.isNotEmpty()) {
                    if (state?.listType == ListType.LIST_TYPE_COLUMN) {
                        LazyColumn(
                            state = lazyColumnState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(items = list, key = { item -> item.file.id }) {
                                ItemFileColumn(
                                    it, visibility
                                ) { itemFile ->
                                    viewModel.onToggle(itemFile)
                                }
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            modifier = Modifier.fillMaxSize(),
                            columns = GridCells.Adaptive(100.dp)
                        ) {
                            items(list) {
                                ItemFileGrid(
                                    it,
                                    visibility
                                ) { itemFile ->
                                    viewModel.onToggle(itemFile)
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            modifier = Modifier
                                .size(100.dp)
                                .alpha(0.5f),
                            painter = painterResource(R.drawable.type_unknown),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You have no encrypted data.",
                            modifier = Modifier.alpha(0.5f)
                        )
                    }
                }
                if (state?.loading == true) {
                    ProgressBar()
                }
            }
        }
    }
}


@Composable
fun ActionButton(modifier: Modifier, label: String, icon: Int) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painterResource(id = icon), contentDescription = null, modifier = Modifier.size(25.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label, fontSize = 12.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    onNavigationItemClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit
) {
    TopAppBar(
        title = {
            Text(text = stringResource(R.string.app_name))
        },
        navigationIcon = {
            IconButton(onClick = onNavigationItemClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Toggle drawer"
                )
            }
        },
        actions = actions
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemFileGrid(
    item: ItemFile, visibility: Float, onToggle: (ItemFile) -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = {
                    Toast
                        .makeText(context, item.file.fileName, Toast.LENGTH_SHORT)
                        .show()
                },
                onLongClick = {
                    onToggle(item)
                }
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Checkbox(
            checked = item.isSelected,
            onCheckedChange = {
                onToggle(item)
            },
            modifier = Modifier
                .animateContentSize()
                .graphicsLayer {
                    alpha = visibility
                }
        )
        Image(
            modifier = Modifier.size(45.dp),
            painter = painterResource(id = item.file.fileType.icon),
            contentDescription = item.file.fileType.type
        )
        Text(
            text = item.file.name,
            fontSize = 18.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Light
        )
        Text(
            text = DateFormatterUtil.formatDate(item.file.createdAt),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )
        )
        Text(
            text = item.file.size,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemFileColumn(
    item: ItemFile, visibility: Float, onToggle: (ItemFile) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(onClick = {}, onLongClick = {
                onToggle(item)
            }), horizontalArrangement = Arrangement.Center
    ) {
        Checkbox(checked = item.isSelected, onCheckedChange = {
            onToggle(item)
        }, modifier = Modifier
            .animateContentSize()
            .graphicsLayer {
                alpha = visibility
            })
        Image(
            modifier = Modifier.size(45.dp),
            painter = painterResource(id = item.file.fileType.icon),
            contentDescription = item.file.fileType.type
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f), verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.file.name,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Light
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = DateFormatterUtil.formatDate(item.file.createdAt),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Light
                    )
                )
                Text(
                    text = item.file.size,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Light
                    )
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Divider(color = Color.Gray.copy(alpha = 0.1f))
        }
    }
}

@Composable
fun DrawerItem(
    modifier: Modifier = Modifier,
    name: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = name
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = name,
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontSize = 18.sp)
                )
            }
        },
        selected = false,
        onClick = {
            onClick()
        }
    )
}